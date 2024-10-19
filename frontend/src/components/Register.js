import React, { useState } from 'react';
import axios from 'axios';

const Register = () => {
    const [userName, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');

    const handleRegister = async (e) => {
        e.preventDefault();


        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }
        try {
            setError(''); // Clear any error messages

            await axios.post('/api/auth/register', { userName, email, password });
            alert('Registration successful!');
        } catch (error) {
            if (error.response) {
                setError(`Registration failed: ${error.response.data || error.response.statusText}`);
                console.error('Registration failed with response:', error.response);
            } else if (error.request) {
                setError('Registration failed: No response received from server');
                console.error('Registration failed without response:', error.request);
            } else {
                setError(`Registration failed: ${error.message}`);
                console.error('Registration error message:', error.message);
            }
        }
    };

    return (
        <div>
            <form onSubmit={handleRegister}>
                <input
                    type="text"
                    placeholder="UserName"
                    value={userName}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Confirm Password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                />
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <button type="submit">Register</button>
            </form>
        </div>
    );
};

export default Register;
