import axios, { AxiosError } from "axios";
import { type FormEvent, type ReactElement, useState } from "react";
import { useNavigate } from "react-router-dom";

const Register = (): ReactElement => {
	const [userName, setUsername] = useState("");
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [confirmPassword, setConfirmPassword] = useState("");
	const [error, setError] = useState("");
	const [successMessage, setSuccessMessage] = useState("");
	const navigate = useNavigate();

	const handleRegister = async (
		e: FormEvent<HTMLFormElement>,
	): Promise<void> => {
		e.preventDefault();

		if (password !== confirmPassword) {
			setError("Passwords do not match");
			return;
		}
		try {
			setError("");

			await axios.post("/api/auth/register", { userName, email, password });
			navigate("/login?success=true");
			setSuccessMessage("Registration successful! Please login.");
		} catch (error) {
			setSuccessMessage("");
			if (error instanceof AxiosError) {
				setError(
					`Registration failed: ${error.response?.data?.error || error.message}`,
				);
				console.error("Registration failed:", error);
			} else if (error instanceof Error) {
				setError(`Registration failed: ${error.message}`);
				console.error("Registration error:", error);
			} else {
				setError("Registration failed: Unknown error occurred");
				console.error("Unknown error:", error);
			}
		}
	};

	return (
		<div className="max-w-sm mx-auto mt-8">
			<form
				onSubmit={handleRegister}
				className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800"
			>
				{successMessage && (
					<p className="text-green-500 mb-4">{successMessage}</p>
				)}

				<div className="mb-4">
					<input
						type="text"
						placeholder="UserName"
						value={userName}
						onChange={(e) => setUsername(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
				</div>

				<div className="mb-4">
					<input
						type="email"
						placeholder="Email"
						value={email}
						onChange={(e) => setEmail(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
				</div>

				<div className="mb-4">
					<input
						type="password"
						placeholder="Password"
						value={password}
						onChange={(e) => setPassword(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
				</div>

				<div className="mb-4">
					<input
						type="password"
						placeholder="Confirm Password"
						value={confirmPassword}
						onChange={(e) => setConfirmPassword(e.target.value)}
						required
						className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
					/>
				</div>

				{error && <p className="text-red-500 mb-4">{error}</p>}

				<button
					type="submit"
					className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800"
				>
					Register
				</button>
			</form>
		</div>
	);
};

export default Register;
