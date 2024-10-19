import logo from './logo.svg';
import './App.css';
import Login from './components/Login';
import Register from './components/Register';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import Home from './components/Home'; 
import NotFound from './components/NotFound'; 
function App() {
    return (
      <Router>
      <nav>
        <Link to="/">Home</Link> | 
        <Link to="/login">Login</Link> | 
        <Link to="/register">Register</Link>
      </nav>
      <Routes>
      <Route path="/" element={<Home />} /> 
      <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="*" element={<NotFound />} /> {/* Catch-all route for 404 */}
      </Routes>
    </Router>
     
    );
  }
  
  export default App;