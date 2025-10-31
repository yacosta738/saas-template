// @ts-check

import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import mermaid from "astro-mermaid";

const patchedRoutingDataPath = fileURLToPath(
	new URL("./src/starlight-patches/routing-data.ts", import.meta.url),
);
const patchedCommonRoutePath = fileURLToPath(
	new URL("./src/starlight-patches/common.astro", import.meta.url),
);
const patchedStaticNotFoundPath = fileURLToPath(
	new URL("./src/starlight-patches/static-404.astro", import.meta.url),
);

// https://astro.build/config
export default defineConfig({
	integrations: [
		mermaid({
			theme: "forest",
			autoTheme: true,
		}),
		starlight({
			title: "Loomify",
			logo: {
				light: "./src/assets/light-isotype.svg",
				dark: "./src/assets/dark-isotype.svg",
			},
			social: [
				{
					icon: "github",
					label: "GitHub",
					href: "https://github.com/yacosta738/saas-template",
				},
				{
					icon: "linkedin",
					label: "LinkedIn",
					href: "https://www.linkedin.com/in/yacosta738/",
				},
			],
			sidebar: [
				{
					label: "🧭 Overview",
					autogenerate: { directory: "overview" },
				},
				{
					label: "🚀 Quick Start Guide",
					autogenerate: { directory: "quick-start" },
				},
				{
					label: "💻 Developer Guide",
					autogenerate: { directory: "developer-guide" },
				},
				{
					label: "⚙️ Configuration",
					autogenerate: { directory: "configuration" },
				},
				{
					label: "📜 Conventions",
					autogenerate: { directory: "conventions" },
				},
				{
					label: "🤝 Contributing",
					link: "/contributing",
				},
				{
					label: "📈 Changelog",
					link: "/changelog",
				},
			],
		}),
	],
	vite: {
		plugins: [
			{
				name: "loomify-starlight-404-patch",
				enforce: "pre",
				load(id) {
					const normalizedId = id.replaceAll("\\", "/");
					if (
						normalizedId.endsWith("/@astrojs/starlight/utils/routing/data.ts")
					) {
						console.info("[starlight] applying patched routing data override");
						return readFileSync(patchedRoutingDataPath, "utf-8");
					}
					if (
						normalizedId.endsWith("/@astrojs/starlight/routes/common.astro")
					) {
						console.info("[starlight] applying patched common route override");
						return readFileSync(patchedCommonRoutePath, "utf-8");
					}
					if (
						normalizedId.endsWith("/@astrojs/starlight/routes/static/404.astro")
					) {
						console.info("[starlight] applying patched static 404 override");
						return readFileSync(patchedStaticNotFoundPath, "utf-8");
					}
					return null;
				},
			},
		],
	},
});
