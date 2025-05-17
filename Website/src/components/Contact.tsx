import React from 'react';
import './Contact.css';

const Contact = () => {
  return (
    <section id="contact" className="contact-section">
      <div className="container">
        <h2>Get in Touch</h2>
        <p className="contact-intro">
          We'd love to hear from you! Whether you have questions, feedback, or press inquiries, please don't hesitate to reach out.
        </p>
        <div className="contact-methods">
          <div className="contact-method">
            <h3>General Inquiries & Support</h3>
            <p>For general questions about Coping Roulette or if you need support with the app, please email us at:</p>
            <a href="mailto:support@copingroulette.com" className="contact-email">support@copingroulette.com</a>
          </div>
          <div className="contact-method">
            <h3>Press & Media</h3>
            <p>For media inquiries, interviews, or access to our media kit, please contact our press team at:</p>
            <a href="mailto:press@copingroulette.com" className="contact-email">press@copingroulette.com</a>
          </div>
        </div>
        {/* Optional: Placeholder for a simple contact form if decided later */}
        {/* <div className="contact-form-placeholder">
          <p><em>Alternatively, you can use the form below (form functionality to be implemented):</em></p>
          <form>
            <input type="text" placeholder="Your Name" />
            <input type="email" placeholder="Your Email" />
            <textarea placeholder="Your Message"></textarea>
            <button type="submit">Send Message</button>
          </form>
        </div> */}
        <p className="contact-note">
          We aim to respond to all inquiries within 48 business hours. Thank you for your interest in Coping Roulette!
        </p>
      </div>
    </section>
  );
};

export default Contact;
