import { WorkspaceId } from "@/workspace";
import type { Workspace } from "../domain/WorkspaceEntity";
import { determineWorkspaceToLoad } from "../domain/WorkspaceSelectionService";
import { workspaceLocalStorage } from "../infrastructure/storage/workspaceLocalStorage";
import { useWorkspaceStore } from "../infrastructure/store/workspaceStore";

const isDev = import.meta.env.DEV;

type WorkspaceStore = ReturnType<typeof useWorkspaceStore>;
type WorkspaceStorage = typeof workspaceLocalStorage;

/**
 * Delays execution for the specified time
 */
function delay(ms: number): Promise<void> {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Composable for workspace loading and switching logic
 */
export function useWorkspaceLoader() {
	/**
	 * Loads workspace on user login following priority rules:
	 * 1. Last selected workspace (if valid)
	 * 2. Default workspace
	 * 3. First available workspace
	 * 4. null (if no workspaces)
	 *
	 * Includes retry logic with exponential backoff: 3 attempts with 1s, 2s, 4s delays
	 */
	async function loadWorkspaceOnLogin(
		userId: string,
		options?: {
			store?: WorkspaceStore;
			storage?: WorkspaceStorage;
			maxRetries?: number;
		},
	): Promise<Workspace | null> {
		const store = options?.store ?? useWorkspaceStore();
		const storage = options?.storage ?? workspaceLocalStorage;
		const maxRetries = options?.maxRetries ?? 3;

		let lastError: Error = new Error("Unknown error during workspace loading");

		// Retry loop with exponential backoff
		for (let attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				if (isDev && attempt > 1) {
					console.log(
						`[Workspace] Retry attempt ${attempt}/${maxRetries} for user ${userId}`,
					);
				}

				// Load all workspaces
				await store.loadWorkspaces();

				if (isDev) {
					console.log(
						`[Workspace] Loaded ${store.workspaces.length} workspaces for user ${userId}`,
					);
				}

				// Validate userId format before attempting load
				if (!WorkspaceId.isValid(userId)) {
					throw new Error(`Invalid user ID format: ${userId}`);
				}
				// Get last selected from local storage
				const lastSelected = storage.getLastSelected(userId);
				const lastSelectedId = lastSelected?.lastSelectedWorkspaceId ?? null;

				// Determine which workspace to load
				const workspaceToLoad = determineWorkspaceToLoad(
					store.workspaces,
					lastSelectedId,
				);

				// Set as current workspace if found
				if (workspaceToLoad) {
					store.setCurrentWorkspace(workspaceToLoad);
					if (isDev) {
						console.log(
							`[Workspace] Auto-loaded workspace: ${workspaceToLoad.name} (ID: ${workspaceToLoad.id})`,
						);
					}
				} else if (isDev) {
					console.warn(`[Workspace] No workspace available for user ${userId}`);
				}

				return workspaceToLoad;
			} catch (error) {
				lastError = error instanceof Error ? error : new Error(String(error));

				if (isDev) {
					console.error(
						`[Workspace] Load attempt ${attempt}/${maxRetries} failed:`,
						lastError.message,
					);
				}

				// If not the last attempt, wait before retrying with exponential backoff
				if (attempt < maxRetries) {
					const delayMs = 2 ** (attempt - 1) * 1000; // 1s, 2s, 4s
					if (isDev) {
						console.log(
							`[Workspace] Waiting ${delayMs}ms before retry ${attempt + 1}`,
						);
					}
					await delay(delayMs);
				}
			}
		}

		// All retries failed
		if (isDev) {
			console.error(
				`[Workspace] Failed to load workspace after ${maxRetries} attempts`,
				lastError,
			);
		}

		throw lastError;
	}

	/**
	 * Switches to a different workspace
	 */
	async function switchWorkspace(
		userId: string,
		workspaceId: string,
		options?: {
			store?: WorkspaceStore;
			storage?: WorkspaceStorage;
		},
	): Promise<void> {
		const store = options?.store ?? useWorkspaceStore();
		const storage = options?.storage ?? workspaceLocalStorage;

		// Find the workspace
		const workspace = store.workspaces.find(
			(w: Workspace) => w.id === workspaceId,
		);
		if (!workspace) {
			throw new Error(`Workspace with ID ${workspaceId} not found`);
		}

		// Set as current workspace
		store.setCurrentWorkspace(workspace);

		// Save to local storage
		storage.saveLastSelected(userId, workspaceId);
	}

	return {
		loadWorkspaceOnLogin,
		switchWorkspace,
	};
}
