import logo from './logo.svg';
import './App.css';
import './index.css';
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './hooks/AuthContext';
import Login from './components/Login';
import Register from './components/Register';
import Home from './components/Home';
import NotFound from './components/NotFound';
import Footer from './components/Footer';
import Header from './components/Header';
import CreateTask from './components/CreateTask';
import ProtectedRoute from './hooks/ProtectedRoute'; // Import the ProtectedRoute component
function App() {
    return (
        <AuthProvider>
            <Router>
                <div className="flex flex-col min-h-screen">
                    <Header />
                    <main className="flex-grow p-6"> {/* takes up the remaining space so footer can stay on bottom of page */}
                        <Routes>
                            <Route path="/" element={<Home />} />
                            
                            <Route 
    path="/createTask" 
    element={
        <ProtectedRoute>
            <CreateTask /> {/* This is the protected CreateTask component */}
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
            </Router>
        </AuthProvider>
    );
}

export default App;
