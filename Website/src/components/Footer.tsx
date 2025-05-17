import React from 'react';
import './Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-content">
        <p>&copy; 2025 Coping Roulette. All rights reserved.</p>
        <div className="footer-links">
          <a href="#privacy">Privacy Policy</a>
          <a href="#terms">Terms of Service</a>
        </div>
        <div className="footer-social">
          {/* Placeholder for social media icons/links */}
          <a href="#facebook" aria-label="Facebook">F</a> 
          <a href="#twitter" aria-label="Twitter">T</a>
          <a href="#instagram" aria-label="Instagram">I</a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
