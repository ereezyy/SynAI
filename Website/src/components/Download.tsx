import React from 'react';
import './Download.css';

// Assume app store badge images are in public/images or similar, or use SVGs
// For this example, using text placeholders for badges.

const Download = () => {
  return (
    <section id="download" className="download-section">
      <div className="container">
        <h2>Start Your Journey to Calm Today</h2>
        <p className="download-subtitle">
          Take the first step towards better mental well-being. Download Coping Roulette and discover a world of personalized support, right at your fingertips.
        </p>
        <div className="download-buttons">
          <a 
            href="#app-store-link" // Replace with actual App Store link
            className="download-button app-store-button"
            target="_blank"
            rel="noopener noreferrer"
          >
            {/* <img src="/images/app-store-badge.svg" alt="Download on the App Store" /> */}
            Download on the App Store
          </a>
          <a 
            href="#google-play-link" // Replace with actual Google Play link
            className="download-button google-play-button"
            target="_blank"
            rel="noopener noreferrer"
          >
            {/* <img src="/images/google-play-badge.svg" alt="Get it on Google Play" /> */}
            Get it on Google Play
          </a>
        </div>
        <div className="app-preview-placeholder">
          {/* Placeholder for an app mockup image or animation */}
          <p>[App Preview Mockup Placeholder]</p>
        </div>
        <p className="availability-note">
          Coping Roulette is available for iOS and Android devices. Core features are free, with an optional premium subscription for advanced functionalities.
        </p>
      </div>
    </section>
  );
};

export default Download;
