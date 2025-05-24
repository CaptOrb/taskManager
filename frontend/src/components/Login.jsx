import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../hooks/AuthContext';
import { useNavigate, useLocation, Link } from 'react-router-dom';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login, loggedInUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (loggedInUser) {
      navigate('/');
    }
  }, [loggedInUser, navigate]);

  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const successMessage = query.get('success');

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const response = await axios.post('/api/auth/login', {
        userName: username,
        password: password,
      }, { withCredentials: true });

      const { jwtToken } = response.data;

      localStorage.setItem('accessToken', jwtToken);
      login(jwtToken);

      setErrorMessage('');
      navigate('/');
    } catch (error) {
      console.error('Login failed', error.response ? error.response.data : error);

      if (error.response && error.response.status === 401) {
        setErrorMessage('Invalid username or password');
      } else {
        setErrorMessage('An unexpected error occurred. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <form onSubmit={handleLogin} className="max-w-sm mx-auto">
        {successMessage && <p style={{ color: 'green' }}>Registration complete! You can now log in.</p>}

        <div className="mb-5">
          <label htmlFor="username" className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">
            Username
          </label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
            required
          />
        </div>

        <div className="mb-5">
          <label htmlFor="password" className="block mb-2 text-sm font-medium text-gray-900 dark:text-white">
            Password
          </label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
            required
          />
        </div>

        <button
          type="submit"
          className="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm w-full sm:w-auto px-5 py-2.5 text-center"
          disabled={isLoading}
        >
          {isLoading ? 'Logging in...' : 'Login'}
        </button>

        {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}

        <p className="text-sm font-light text-gray-500 dark:text-gray-400">
          Don’t have an account yet?
          <Link to="/register" className="font-medium text-primary-600 hover:underline dark:text-primary-500">
            {' '}Sign up
          </Link>
        </p>
      </form>
    </div>
  );
}

export default Login;
