export function getValidRedirectPath(path: string | null): string {
	if (
		!path ||
		!path.startsWith("/") ||
		path.startsWith("//") ||
		path.includes("://") ||
		path.includes("\\")
	) {
		return "/";
	}
	return path;
}
