import React, { useState, useEffect } from 'react';
import axios from 'axios';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [greeting, setGreeting] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // Extract username from JWT token on initial render (if token is present)
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      // Decode the token and extract the subject (username)
      const decodedToken = JSON.parse(atob(token.split('.')[1]));
      
      // 'sub' is typically the username or user ID in JWT tokens
      const usernameFromToken = decodedToken.sub;
      
      // Set a friendly greeting message
      setGreeting(`Hello, ${usernameFromToken}`);
    }
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.post('/api/auth/login', {
        userName: username,
        password: password
      });

      const { jwtToken } = response.data;
      localStorage.setItem('token', jwtToken);

      const decodedToken = JSON.parse(atob(jwtToken.split('.')[1]));
      const usernameFromToken = decodedToken.sub; 

      setGreeting(`Hello, ${usernameFromToken}`);
      setErrorMessage(''); 
    } catch (error) {
      console.error("Login failed", error);
      setErrorMessage("Login failed. Check your credentials.");
      setGreeting(''); // Clear greeting on error
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token'); // Clear the token from localStorage
    setGreeting(''); // Clear greeting
    setUsername(''); // Clear username
    setPassword(''); // Clear password
  };

  return (
    <div>
      {greeting ? (
        <div>
          <h2>{greeting}</h2>
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
