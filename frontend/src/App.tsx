import "./App.css";
import "./index.css";
import { Route, Routes } from "react-router-dom";
import CreateTask from "./components/CreateTask";
import Footer from "./components/Footer";
import Header from "./components/Header";
import Home from "./components/Home";
import Login from "./components/Login";
import MyAccount from "./components/MyAccount";
import NotFound from "./components/NotFound";
import Register from "./components/Register";
import TaskDetail from "./components/TaskDetail";
import { AuthProvider } from "./hooks/AuthContext";
import ProtectedRoute from "./hooks/ProtectedRoute";

function App(): JSX.Element {
	return (
		<AuthProvider>
			<div className="flex flex-col min-h-screen">
				<Header />
				<main className="flex-grow p-6">
					<Routes>
						<Route path="/" element={<Home />} />
						<Route
							path="/createTask"
							element={
								<ProtectedRoute>
									<CreateTask />
								</ProtectedRoute>
							}
						/>
						<Route
							path="/account"
							element={
								<ProtectedRoute>
									<MyAccount />
								</ProtectedRoute>
							}
						/>
						<Route
							path="/tasks/:id"
							element={
								<ProtectedRoute>
									<TaskDetail />
								</ProtectedRoute>
							}
						/>
						<Route path="/login" element={<Login />} />
						<Route path="/register" element={<Register />} />
						<Route path="*" element={<NotFound />} />
					</Routes>
				</main>
				<Footer />
			</div>
		</AuthProvider>
	);
}

export default App;
