import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Workspace } from "../../../domain/WorkspaceEntity";
import { workspaceHttpClient } from "../../http/workspaceHttpClient";

// Use the HTTP client as the API client for these tests
const workspaceApiClient = workspaceHttpClient;

// Type for accessing protected methods in tests
type HttpClientWithGet = typeof workspaceApiClient & {
	get: (url: string, config?: unknown) => Promise<unknown>;
};

describe("workspaceApiClient", () => {
	const mockWorkspace1: Workspace = {
		id: "550e8400-e29b-41d4-a716-446655440000",
		name: "Workspace 1",
		description: "First workspace",
		isDefault: false,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-01T10:00:00Z"),
		updatedAt: new Date("2025-10-01T10:00:00Z"),
	};

	const mockWorkspace2: Workspace = {
		id: "660e8400-e29b-41d4-a716-446655440001",
		name: "Workspace 2",
		description: "Default workspace",
		isDefault: true,
		ownerId: "123e4567-e89b-42d3-a456-426614174000",
		createdAt: new Date("2025-10-02T10:00:00Z"),
		updatedAt: new Date("2025-10-02T10:00:00Z"),
	};

	beforeEach(() => {
		vi.clearAllMocks();
	});

	describe("getAllWorkspaces", () => {
		it("should return array of workspaces from API", async () => {
			// Mock the get method
			const mockGet = vi.fn().mockResolvedValue({
				data: [mockWorkspace1, mockWorkspace2],
			});
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			const result = await workspaceApiClient.getAllWorkspaces();

			expect(result).toEqual([mockWorkspace1, mockWorkspace2]);
			expect(result).toHaveLength(2);
		});

		it("should return empty array when no workspaces exist", async () => {
			const mockGet = vi.fn().mockResolvedValue({ data: [] });
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			const result = await workspaceApiClient.getAllWorkspaces();

			expect(result).toEqual([]);
			expect(result).toHaveLength(0);
		});

		it("should throw error when network fails", async () => {
			const mockGet = vi.fn().mockRejectedValue(new Error("Network failure"));
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			await expect(workspaceApiClient.getAllWorkspaces()).rejects.toThrow();
		});

		it("should throw error when API returns error", async () => {
			const apiError = {
				response: {
					data: {
						error: {
							code: "INTERNAL_ERROR",
							message: "Internal server error",
						},
					},
				},
			};
			const mockGet = vi.fn().mockRejectedValue(apiError);
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			await expect(workspaceApiClient.getAllWorkspaces()).rejects.toThrow();
		});

		it("should parse dates correctly from API response", async () => {
			const mockApiWorkspace = {
				...mockWorkspace1,
				createdAt: "2025-10-01T10:00:00Z",
				updatedAt: "2025-10-01T10:00:00Z",
			};
			const mockGet = vi.fn().mockResolvedValue({
				data: [mockApiWorkspace],
			});
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			const result = await workspaceApiClient.getAllWorkspaces();

			expect(result[0]?.createdAt).toBeInstanceOf(Date);
			expect(result[0]?.updatedAt).toBeInstanceOf(Date);
			expect(result[0]?.createdAt.toISOString()).toBe(
				"2025-10-01T10:00:00.000Z",
			);
		});
	});

	describe("getWorkspace", () => {
		it("should return workspace by ID from API", async () => {
			const mockGet = vi.fn().mockResolvedValue({
				data: mockWorkspace1,
			});
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			const result = await workspaceApiClient.getWorkspace(mockWorkspace1.id);

			expect(result).toEqual(mockWorkspace1);
		});

		it("should return null when workspace not found", async () => {
			const mockGet = vi.fn().mockResolvedValue({ data: null });
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			const result = await workspaceApiClient.getWorkspace(
				"999e8400-e29b-41d4-a716-446655440999",
			);

			expect(result).toBeNull();
		});

		it("should throw error when network fails", async () => {
			const mockGet = vi.fn().mockRejectedValue(new Error("Network failure"));
			// Cast to access protected get method for testing
			vi.spyOn(
				workspaceApiClient as HttpClientWithGet,
				"get",
			).mockImplementation(mockGet);

			await expect(
				workspaceApiClient.getWorkspace(mockWorkspace1.id),
			).rejects.toThrow();
		});
	});
});
