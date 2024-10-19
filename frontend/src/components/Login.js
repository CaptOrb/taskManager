import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthContext';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const { loggedInUser, login, logout } = useAuth();

    const handleLogin = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post('/api/auth/login', {
                userName: username,
                password: password
            });

            const { jwtToken } = response.data;
            login(jwtToken);
            setErrorMessage('');
        } catch (error) {
            console.error("Login failed", error);
            setErrorMessage("Login failed. Check your credentials.");
        }
    };

    const handleLogout = () => {
        logout();
        setUsername('');
        setPassword('');
    };

    return (
        <div>
            {loggedInUser ? (
                <div>
                    <h2>Hello, {loggedInUser}</h2>
                    <button onClick={handleLogout}>Logout</button>
                </div>
            ) : (
                <div>
                    <h1>Login</h1>
                    <form onSubmit={handleLogin}>
                        <div>
                            <label>Username:</label>
                            <input
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                            />
                        </div>
                        <div>
                            <label>Password:</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>
                        <button type="submit">Login</button>
                    </form>

                    {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
                </div>
            )}
        </div>
    );
}

export default Login;
