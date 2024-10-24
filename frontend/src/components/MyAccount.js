import React, { useState, useEffect } from 'react';

const MyAccount = () => {
    const [userName, setUserName] = useState('');
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                const response = await fetch('/api/auth/current-user', 
                    {
                        headers: {
                          Authorization: `Bearer ${localStorage.getItem('token')}` // Add JWT token to the request
                        }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch user');
                }

                const data = await response.json();
                setUserName(data.userName);
                setEmail(data.email);
                setLoading(false);
            } catch (error) {
                setError(error.message);
                setLoading(false);
            }
        };

        fetchCurrentUser();
    }, []);  

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="max-w-sm mx-auto mt-8">
        <form className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800">

            <label htmlFor="userName" className="block text-sm font-medium text-gray-700 dark:text-white">
                Username
            </label>
            <input
                type="text"
                id="userName"
                value={userName}
                disabled
                className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
            />

            <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-white">
                Email
            </label>
            <input
                type="email"
                id="email"
                value={email}
                disabled
                className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
            />

          <button
            className="text-white bg-red-700 hover:bg-red-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
          >
            Cancel
          </button>
        </form>
        </div>
    
    );
};

export default MyAccount;
