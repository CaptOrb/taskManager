import type { ReactElement } from "react";
import { Link } from "react-router-dom";

const NotFound = (): ReactElement => {
	return (
		<div className="flex flex-col items-center justify-center">
			<h1 className="text-6xl font-bold text-gray-800 dark:text-white">404</h1>
			<p className="mt-4 text-xl text-gray-600 dark:text-gray-400">
				Oops! The page you&#39;re looking for doesn&#39;t exist.
			</p>
			<Link
				to="/"
				className="mt-6 text-blue-600 hover:underline dark:text-blue-400"
			>
				Go back to Home
			</Link>
		</div>
	);
};

export default NotFound;
