import React from 'react';
import './Press.css';

const Press = () => {
  return (
    <section id="press" className="press-section">
      <div className="container">
        <h2>Coping Roulette in the News</h2>
        <div className="press-content">
          <p>
            Stay updated with the latest mentions and news about Coping Roulette. We are excited to share our journey and the impact we aim to make in the mental wellness space.
          </p>
          <div className="press-item">
            <h3>FOR IMMEDIATE RELEASE: Coping Roulette App Launch</h3>
            <p>Read our official launch announcement to learn more about our mission, features, and commitment to accessible mental wellness.</p>
            {/* Link to the press release document. For now, it's a placeholder. 
                In a real scenario, this would link to a PDF or a dedicated press release page. */}
            <a href="/Coping_Roulette_Press_Release.md" target="_blank" rel="noopener noreferrer" className="press-link">
              Read Full Press Release (PDF/Markdown)
            </a>
            <p className="press-note">
              (Note: This link will attempt to open the Markdown file. For a live website, this would typically be a hosted PDF or a separate HTML page.)
            </p>
          </div>
          <div className="media-kit-info">
            <h4>Media Kit</h4>
            <p>
              Members of the press can access our media kit for logos, screenshots (to be added upon app finalization), and other relevant materials by contacting us at <a href="mailto:press@copingroulette.com">press@copingroulette.com</a>.
            </p>
          </div>
          {/* Placeholder for future press mentions */}
          <div className="future-press-placeholder">
            <p><em>More press mentions coming soon...</em></p>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Press;
