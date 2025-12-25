import type { ReactElement } from "react";
import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../hooks/auth-context";

const Header = (): ReactElement => {
	const { loggedInUser, logout } = useAuth();
	const [dropdownOpen, setDropdownOpen] = useState(false);
	const dropdownRef = useRef<HTMLDivElement>(null);

	const toggleDropdown = (): void => {
		setDropdownOpen((prevState) => !prevState);
	};

	// Close dropdown when clicking outside
	useEffect(() => {
		const handleClickOutside = (event: MouseEvent): void => {
			if (
				dropdownRef.current &&
				!dropdownRef.current.contains(event.target as Node)
			) {
				setDropdownOpen(false);
			}
		};

		if (dropdownOpen) {
			document.addEventListener("mousedown", handleClickOutside);
		}

		return (): void => {
			document.removeEventListener("mousedown", handleClickOutside);
		};
	}, [dropdownOpen]);

	return (
		<header className="pb-4 mb-2">
			<nav className="border-gray-200 bg-gray-50 dark:bg-gray-800 dark:border-gray-800">
				<div className="max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4">
					<Link to="/" className="flex items-center">
						<img
							src="https://flowbite.com/docs/images/logo.svg"
							className="mr-3 h-6 sm:h-9"
							alt="Flowbite Logo"
						/>
						<span className="self-center text-xl font-semibold whitespace-nowrap dark:text-white">
							Task Management App
						</span>
					</Link>

					{loggedInUser ? (
						<div className="relative" ref={dropdownRef}>
							<button
								type="button"
								className="flex text-sm rounded-full focus:ring-4 focus:ring-gray-300"
								onClick={toggleDropdown}
							>
								<span className="sr-only">Open user menu</span>
								<svg
									className="w-6 h-6 text-gray-800 dark:text-white"
									aria-hidden="true"
									xmlns="http://www.w3.org/2000/svg"
									width="24"
									height="24"
									fill="currentColor"
									viewBox="0 0 24 24"
								>
									<path
										fillRule="evenodd"
										d="M12 20a7.966 7.966 0 0 1-5.002-1.756l.002.001v-.683c0-1.794 1.492-3.25 3.333-3.25h3.334c1.84 0 3.333 1.456 3.333 3.25v.683A7.966 7.966 0 0 1 12 20ZM2 12C2 6.477 6.477 2 12 2s10 4.477 10 10c0 5.5-4.44 9.963-9.932 10h-.138C6.438 21.962 2 17.5 2 12Zm10-5c-1.84 0-3.333 1.455-3.333 3.25S10.159 13.5 12 13.5c1.84 0 3.333-1.455 3.333-3.25S13.841 7 12 7Z"
										clipRule="evenodd"
									/>
								</svg>
							</button>

							{dropdownOpen && (
								<div className="absolute left-1/2 transform -translate-x-1/2 mt-2 w-48 bg-white divide-y divide-gray-100 rounded-lg shadow-lg dark:bg-gray-700 dark:divide-gray-500">
									<div className="px-4 py-3">
										<span className="block text-sm text-gray-900 dark:text-white">
											{loggedInUser}
										</span>
									</div>
									<ul className="py-2">
										<li>
											<Link
												to="/account"
												className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-gray-200 dark:hover:text-white"
											>
												My Account
											</Link>
										</li>
										<li>
											<Link
												to="/"
												onClick={logout}
												className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-600 dark:text-gray-200 dark:hover:text-white"
											>
												Logout
											</Link>
										</li>
									</ul>
								</div>
							)}
						</div>
					) : (
						<div className="flex items-center lg:order-2">
							<Link
								to="/login"
								className="text-gray-800 dark:text-white hover:bg-gray-50 focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-4 lg:px-5 py-2 lg:py-2.5 mr-2 dark:hover:bg-gray-700 focus:outline-none dark:focus:ring-gray-800"
							>
								Log in
							</Link>
							<Link
								to="/register"
								className="text-gray-800 dark:text-white hover:bg-gray-50 focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-4 lg:px-5 py-2 lg:py-2.5 mr-2 dark:hover:bg-gray-700 focus:outline-none dark:focus:ring-gray-800"
							>
								Register
							</Link>
						</div>
					)}
				</div>
			</nav>
		</header>
	);
};

export default Header;
