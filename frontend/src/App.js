import logo from './logo.svg';
import './App.css';
import React from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './components/AuthContext';
import Login from './components/Login';
import Register from './components/Register';
import Home from './components/Home'; 
import NotFound from './components/NotFound'; 

const Navigation = () => {
    const { loggedInUser, logout } = useAuth();

    return (
        <nav>
            <Link to="/">Home</Link> | 
            {loggedInUser ? (
                <>
                    <span>Hello, {loggedInUser}</span> 
                </>
            ) : (
                <>
                    <Link to="/login">Login</Link> | 
                    <Link to="/register">Register</Link>
                </>
            )}
        </nav>
    );
};

function App() {
    return (
        <AuthProvider>
            <Router>
                <Navigation /> 
                <Routes>
                    <Route path="/" element={<Home />} /> 
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="*" element={<NotFound />} />
                    
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
