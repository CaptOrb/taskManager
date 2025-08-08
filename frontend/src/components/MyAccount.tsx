import { useState, useEffect, FormEvent, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';

const MyAccount = () => {
    const [userName, setUserName] = useState('');
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [showPasswordChange, setShowPasswordChange] = useState<boolean>(false);
    const [passwordChangeLoading, setPasswordChangeLoading] = useState<boolean>(false);
    const [passwordChangeError, setPasswordChangeError] = useState<string | null>(null);
    const [passwordChangeSuccess, setPasswordChangeSuccess] = useState<boolean>(false);
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const navigate = useNavigate();

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
                setError(error instanceof Error ? error.message : 'Unknown error occurred');
                setLoading(false);
            }
        };

        fetchCurrentUser();
    }, []);  

    const handlePasswordChange = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setPasswordChangeLoading(true);
        setPasswordChangeError(null);
        setPasswordChangeSuccess(false);

        // Validate passwords match
        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            setPasswordChangeError('New password and confirmation password do not match');
            setPasswordChangeLoading(false);
            return;
        }

        // Validate password length
        if (passwordForm.newPassword.length < 7) {
            setPasswordChangeError('New password must be at least 7 characters long');
            setPasswordChangeLoading(false);
            return;
        }

        try {
            const response = await fetch('/api/auth/change-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(passwordForm)
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to change password');
            }

            setPasswordChangeSuccess(true);
            setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
            setShowPasswordChange(false);
            
            // Clear success message after 3 seconds
            setTimeout(() => {
                setPasswordChangeSuccess(false);
            }, 3000);

        } catch (error) {
            setPasswordChangeError(error instanceof Error ? error.message : 'Unknown error occurred');
        } finally {
            setPasswordChangeLoading(false);
        }
    };

    const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setPasswordForm(prev => ({
            ...prev,
            [name]: value
        }));
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="max-w-sm mx-auto mt-8">
            <div className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4 dark:bg-gray-800">
                <label htmlFor="userName" className="block text-sm font-medium text-gray-700 dark:text-white">
                    Username
                </label>
                <input
                    type="text"
                    id="userName"
                    value={userName}
                    disabled
                    aria-label="Username"
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
                    aria-label="Email"
                    className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                />

                {passwordChangeSuccess && (
                    <div className="mb-4 p-4 text-sm text-green-800 border border-green-300 rounded-lg bg-green-50 dark:bg-gray-800 dark:text-green-400 dark:border-green-800">
                        Password changed successfully!
                    </div>
                )}

                {!showPasswordChange ? (
                    <button
                        type="button"
                        onClick={() => setShowPasswordChange(true)}
                        className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2 mr-2"
                    >
                        Change Password
                    </button>
                ) : (
                    <div className="mb-4 p-4 border border-gray-300 rounded-lg bg-gray-50 dark:bg-gray-700 dark:border-gray-600">
                        <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">Change Password</h3>
                        
                        {passwordChangeError && (
                            <div className="mb-4 p-4 text-sm text-red-800 border border-red-300 rounded-lg bg-red-50 dark:bg-gray-800 dark:text-red-400 dark:border-red-800">
                                {passwordChangeError}
                            </div>
                        )}

                        <form onSubmit={handlePasswordChange} method="POST">
                            <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 dark:text-white">
                                Current Password
                            </label>
                            <input
                                type="password"
                                id="currentPassword"
                                name="currentPassword"
                                value={passwordForm.currentPassword}
                                onChange={handleInputChange}
                                required
                                className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                            />

                            <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 dark:text-white">
                                New Password
                            </label>
                            <input
                                type="password"
                                id="newPassword"
                                name="newPassword"
                                value={passwordForm.newPassword}
                                onChange={handleInputChange}
                                required
                                className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                            />

                            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 dark:text-white">
                                Confirm New Password
                            </label>
                            <input
                                type="password"
                                id="confirmPassword"
                                name="confirmPassword"
                                value={passwordForm.confirmPassword}
                                onChange={handleInputChange}
                                required
                                className="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 mb-2 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                            />

                            <div className="flex gap-2">
                                <button
                                    type="submit"
                                    disabled={passwordChangeLoading}
                                    className="text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2 disabled:opacity-50"
                                >
                                    {passwordChangeLoading ? 'Changing...' : 'Change Password'}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowPasswordChange(false);
                                        setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
                                        setPasswordChangeError(null);
                                    }}
                                    className="text-white bg-gray-700 hover:bg-gray-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
                                >
                                    Cancel
                                </button>
                            </div>
                        </form>
                    </div>
                )}

                <button
                    type="button"
                    onClick={() => {
                        if (window.history.length > 1) {
                            navigate(-1); 
                        } else {
                            navigate('/');
                        }
                    }}
                    className="text-white bg-red-700 hover:bg-red-800 font-medium rounded-lg text-sm px-5 py-2.5 mb-2"
                >
                    Cancel
                </button>
            </div>
        </div>
    );
};

export default MyAccount;
