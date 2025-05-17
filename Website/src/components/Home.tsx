import React from 'react';
import './Home.css';

const Home = () => {
  return (
    <section id="home" className="hero-section">
      <div className="hero-content">
        <h1>Find Your Calm: Discover Personalized Coping Strategies with Coping Roulette.</h1>
        <p>Navigate stress and anxiety with our supportive mental wellness app. Spin the wheel, chat with our AI companion, and find local resources – all in one place.</p>
        <div className="hero-cta">
          <a href="#download-appstore" className="cta-button app-store">Download on the App Store</a>
          <a href="#download-googleplay" className="cta-button google-play">Get it on Google Play</a>
        </div>
      </div>
      {/* Placeholder for engaging visual, could be an image or animation */}
      <div className="hero-visual-placeholder">
         {/* <img src={heroImage} alt="Coping Roulette App Mockup" /> */}
         <p>[Engaging Visual Placeholder - e.g., App Mockup or Abstract Calming Design]</p>
      </div>
    </section>
  );
};

export default Home;
