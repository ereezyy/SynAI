import React from 'react';
import './Navbar.css';
import logo from '../assets/logo.png'; // Adjusted path

const Navbar = () => {
  return (
    <nav className="navbar">
      <div className="navbar-logo">
        <img src={logo} alt="Coping Roulette Logo" />
        <span>Coping Roulette</span>
      </div>
      <ul className="navbar-links">
        <li><a href="#home">Home</a></li>
        <li><a href="#features">Features</a></li>
        <li><a href="#about">About Us</a></li>
        <li><a href="#press">Press</a></li>
        <li><a href="#download">Download</a></li>
        <li><a href="#contact">Contact</a></li>
      </ul>
    </nav>
  );
};

export default Navbar;
