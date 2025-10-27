// TinyAmp Neural Audio Player - Main Application
// Hybrid Android WebView + Native ExoPlayer Implementation

class TinyAmpApp {
    constructor() {
        this.isPlaying = false;
        this.currentTrack = null;
        this.playlist = [];
        this.currentIndex = 0;
        this.audioContext = null;
        this.analyser = null;
        this.dataArray = null;
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.visualizers = [];
        this.bridgeReady = false;

        this.init();
    }

    async init() {
        console.log('TinyAmp initializing...');

        // Check for Android bridge
        this.checkAndroidBridge();

        // Initialize Three.js visualizer
        this.initThreeJS();

        // Load default playlist
        this.loadDefaultPlaylist();

        // Start animation loop
        this.animate();

        // Initialize audio context (for Web Audio API visualization)
        this.initAudioContext();

        console.log('TinyAmp initialized');
    }

    checkAndroidBridge() {
        if (typeof Android !== 'undefined') {
            this.bridgeReady = true;
            console.log('Android bridge detected');
            this.updateStatus('bridge-status', 'Bridge: Connected', true);

            // Listen for Android bridge ready event
            window.addEventListener('androidBridgeReady', () => {
                console.log('Android bridge ready event received');
            });
        } else {
            console.log('Running in browser mode (no Android bridge)');
            this.updateStatus('bridge-status', 'Bridge: Browser', false);
        }
    }

    initThreeJS() {
        const container = document.getElementById('canvas-container');

        // Scene
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0x000000);
        this.scene.fog = new THREE.Fog(0x000000, 10, 50);

        // Camera
        this.camera = new THREE.PerspectiveCamera(
            75,
            window.innerWidth / window.innerHeight,
            0.1,
            1000
        );
        this.camera.position.z = 15;

        // Renderer
        this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        this.renderer.setPixelRatio(window.devicePixelRatio);
        container.appendChild(this.renderer.domElement);

        // Lighting
        const ambientLight = new THREE.AmbientLight(0x404040, 0.5);
        this.scene.add(ambientLight);

        const pointLight = new THREE.PointLight(0x00FFFF, 1, 100);
        pointLight.position.set(0, 10, 10);
        this.scene.add(pointLight);

        const pointLight2 = new THREE.PointLight(0xFF00FF, 1, 100);
        pointLight2.position.set(0, -10, 10);
        this.scene.add(pointLight2);

        // Create audio-reactive visualizers
        this.createVisualizers();

        // Handle window resize
        window.addEventListener('resize', () => this.onWindowResize());

        this.updateStatus('viz-status', 'Viz: Active', true);
    }

    createVisualizers() {
        // Central reactive cube
        const geometry = new THREE.BoxGeometry(2, 2, 2);
        const material = new THREE.MeshPhongMaterial({
            color: 0x00FFFF,
            emissive: 0x00FFFF,
            emissiveIntensity: 0.2,
            wireframe: false,
            transparent: true,
            opacity: 0.8
        });
        const cube = new THREE.Mesh(geometry, material);
        this.scene.add(cube);
        this.visualizers.push({ mesh: cube, type: 'cube', speed: 0.01 });

        // Wireframe overlay
        const wireframeGeo = new THREE.BoxGeometry(2.1, 2.1, 2.1);
        const wireframeMat = new THREE.MeshBasicMaterial({
            color: 0xFF00FF,
            wireframe: true,
            transparent: true,
            opacity: 0.5
        });
        const wireframe = new THREE.Mesh(wireframeGeo, wireframeMat);
        this.scene.add(wireframe);
        this.visualizers.push({ mesh: wireframe, type: 'wireframe', speed: -0.005 });

        // Particle field
        const particlesGeometry = new THREE.BufferGeometry();
        const particleCount = 500;
        const positions = new Float32Array(particleCount * 3);

        for (let i = 0; i < particleCount * 3; i++) {
            positions[i] = (Math.random() - 0.5) * 50;
        }

        particlesGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

        const particlesMaterial = new THREE.PointsMaterial({
            color: 0x00FFFF,
            size: 0.1,
            transparent: true,
            opacity: 0.6
        });

        const particles = new THREE.Points(particlesGeometry, particlesMaterial);
        this.scene.add(particles);
        this.visualizers.push({ mesh: particles, type: 'particles', speed: 0.0005 });

        // Outer rings
        for (let i = 0; i < 3; i++) {
            const ringGeo = new THREE.TorusGeometry(5 + i * 2, 0.1, 16, 100);
            const ringMat = new THREE.MeshBasicMaterial({
                color: i % 2 === 0 ? 0x00FFFF : 0xFF00FF,
                transparent: true,
                opacity: 0.3
            });
            const ring = new THREE.Mesh(ringGeo, ringMat);
            ring.rotation.x = Math.PI / 2;
            this.scene.add(ring);
            this.visualizers.push({
                mesh: ring,
                type: 'ring',
                speed: 0.002 * (i + 1),
                offset: i * Math.PI / 3
            });
        }
    }

    animate() {
        requestAnimationFrame(() => this.animate());

        // Get audio data if available
        let audioLevel = 0;
        if (this.analyser && this.dataArray) {
            this.analyser.getByteFrequencyData(this.dataArray);
            const average = this.dataArray.reduce((a, b) => a + b) / this.dataArray.length;
            audioLevel = average / 255;

            // Update resonance meter
            this.updateResonanceMeter(audioLevel);

            // Send audio data to Android (for potential native visualization sync)
            if (this.bridgeReady && this.isPlaying) {
                // Throttle: only send every 10th frame
                if (Math.random() < 0.1) {
                    const audioData = Array.from(this.dataArray.slice(0, 32)); // Send first 32 bins
                    Android.onAudioAnalysisData(JSON.stringify(audioData));
                }
            }
        } else {
            // Simulate audio reactivity when no audio data
            audioLevel = Math.sin(Date.now() * 0.002) * 0.5 + 0.5;
        }

        // Animate visualizers
        this.visualizers.forEach((viz, index) => {
            const { mesh, type, speed, offset = 0 } = viz;
            const time = Date.now() * 0.001;

            switch (type) {
                case 'cube':
                    mesh.rotation.x += speed;
                    mesh.rotation.y += speed * 1.5;
                    mesh.scale.setScalar(1 + audioLevel * 0.3);
                    mesh.material.emissiveIntensity = 0.2 + audioLevel * 0.5;
                    break;

                case 'wireframe':
                    mesh.rotation.x += speed;
                    mesh.rotation.y += speed * 1.5;
                    mesh.scale.setScalar(1.05 + audioLevel * 0.35);
                    break;

                case 'particles':
                    mesh.rotation.y += speed;
                    const positions = mesh.geometry.attributes.position.array;
                    for (let i = 0; i < positions.length; i += 3) {
                        positions[i + 1] += Math.sin(time + positions[i]) * 0.01 * audioLevel;
                    }
                    mesh.geometry.attributes.position.needsUpdate = true;
                    break;

                case 'ring':
                    mesh.rotation.z += speed;
                    mesh.scale.setScalar(1 + Math.sin(time + offset) * 0.1 * audioLevel);
                    mesh.material.opacity = 0.3 + audioLevel * 0.2;
                    break;
            }
        });

        // Camera movement
        this.camera.position.x = Math.sin(Date.now() * 0.0003) * 2;
        this.camera.position.y = Math.cos(Date.now() * 0.0004) * 2;
        this.camera.lookAt(this.scene.position);

        this.renderer.render(this.scene, this.camera);
    }

    onWindowResize() {
        this.camera.aspect = window.innerWidth / window.innerHeight;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(window.innerWidth, window.innerHeight);
    }

    initAudioContext() {
        // Create Web Audio API context for visualization
        // Note: This is separate from native ExoPlayer playback
        // We use this only for frequency analysis visualization
        try {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            this.audioContext = new AudioContext();
            this.analyser = this.audioContext.createAnalyser();
            this.analyser.fftSize = 256;
            this.dataArray = new Uint8Array(this.analyser.frequencyBinCount);
            console.log('Web Audio API initialized for visualization');
        } catch (error) {
            console.warn('Web Audio API not available:', error);
        }
    }

    loadDefaultPlaylist() {
        // Default playlist with Archive.org public domain music
        this.playlist = [
            {
                title: 'Test Track - Sample',
                artist: 'Internet Archive',
                url: 'https://archive.org/download/testmp3testfile/mpthreetest.mp3',
                albumArt: ''
            },
            {
                title: 'Grateful Dead Live',
                artist: 'Grateful Dead',
                url: 'https://archive.org/download/gd1977-05-08.sbd.hicks.4136.sbeok.shnf/gd77-05-08d1t01.mp3',
                albumArt: ''
            }
        ];

        console.log('Default playlist loaded:', this.playlist.length, 'tracks');
    }

    // Playback controls
    togglePlayback() {
        if (this.isPlaying) {
            this.pause();
        } else {
            this.play();
        }
    }

    play() {
        if (!this.currentTrack && this.playlist.length > 0) {
            this.currentTrack = this.playlist[this.currentIndex];
        }

        if (!this.currentTrack) {
            this.showToast('No track to play. Search Archive.org!');
            return;
        }

        const { url, title, artist, albumArt = '' } = this.currentTrack;

        // Use native Android playback
        if (this.bridgeReady) {
            Android.play(url, title, artist, albumArt);
        } else {
            // Fallback: browser mode (not recommended for production)
            console.log('Playing in browser mode:', title);
        }

        this.isPlaying = true;
        this.updatePlaybackUI();
        this.updateStatus('audio-status', 'Audio: Playing', true);

        // Hide visualizer info
        document.getElementById('visualizer-info').style.display = 'none';
    }

    pause() {
        if (this.bridgeReady) {
            Android.pause();
        }

        this.isPlaying = false;
        this.updatePlaybackUI();
        this.updateStatus('audio-status', 'Audio: Paused', false);
    }

    nextTrack() {
        this.currentIndex = (this.currentIndex + 1) % this.playlist.length;
        this.currentTrack = this.playlist[this.currentIndex];

        if (this.isPlaying) {
            this.play();
        } else {
            this.updateNowPlaying();
        }
    }

    previousTrack() {
        this.currentIndex = (this.currentIndex - 1 + this.playlist.length) % this.playlist.length;
        this.currentTrack = this.playlist[this.currentIndex];

        if (this.isPlaying) {
            this.play();
        } else {
            this.updateNowPlaying();
        }
    }

    shuffle() {
        // Fisher-Yates shuffle
        for (let i = this.playlist.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [this.playlist[i], this.playlist[j]] = [this.playlist[j], this.playlist[i]];
        }
        this.currentIndex = 0;
        this.currentTrack = this.playlist[0];
        this.updateNowPlaying();
        this.showToast('Playlist shuffled!');
    }

    updatePlaybackUI() {
        const btn = document.getElementById('play-pause-btn');
        btn.textContent = this.isPlaying ? '⏸️ Pause' : '▶️ Play';
        btn.classList.toggle('btn-primary', !this.isPlaying);

        this.updateNowPlaying();
    }

    updateNowPlaying() {
        if (this.currentTrack) {
            document.getElementById('track-title').textContent = this.currentTrack.title;
            document.getElementById('track-artist').textContent = this.currentTrack.artist;
        }
    }

    updateStatus(elementId, text, active = false) {
        const el = document.getElementById(elementId);
        el.textContent = text;
        el.classList.toggle('active', active);
    }

    updateResonanceMeter(level) {
        const fill = document.getElementById('resonance-fill');
        fill.style.width = (level * 100) + '%';
    }

    // Archive.org search
    async search() {
        const query = document.getElementById('search-input').value.trim();
        if (!query) {
            this.showToast('Enter a search query');
            return;
        }

        const results = document.getElementById('search-results');
        results.innerHTML = '<div style="color: #00FFFF;">🔍 Searching Archive.org...</div>';

        try {
            // Search Archive.org for audio files
            const url = `https://archive.org/advancedsearch.php?q=${encodeURIComponent(query)}+AND+mediatype:audio&fl=identifier,title,creator,format&rows=20&output=json`;

            const response = await fetch(url);
            const data = await response.json();

            if (data.response.docs.length === 0) {
                results.innerHTML = '<div style="color: #FF00FF;">No results found</div>';
                return;
            }

            results.innerHTML = '';

            for (const doc of data.response.docs) {
                const item = document.createElement('div');
                item.className = 'result-item';
                item.innerHTML = `
                    <div class="result-title">${doc.title || 'Unknown Title'}</div>
                    <div class="result-meta">${doc.creator || 'Unknown Artist'}</div>
                `;

                item.onclick = async () => {
                    await this.loadArchiveItem(doc.identifier, doc.title, doc.creator);
                };

                results.appendChild(item);
            }
        } catch (error) {
            console.error('Search error:', error);
            results.innerHTML = '<div style="color: #FF0055;">Search failed. Check connection.</div>';
        }
    }

    async loadArchiveItem(identifier, title, artist) {
        try {
            // Get file details from Archive.org
            const url = `https://archive.org/metadata/${identifier}`;
            const response = await fetch(url);
            const data = await response.json();

            // Find the first MP3 or audio file
            const audioFile = data.files.find(f =>
                f.format === 'VBR MP3' ||
                f.format === 'MP3' ||
                f.format === '128Kbps MP3' ||
                f.name.endsWith('.mp3')
            );

            if (!audioFile) {
                this.showToast('No audio file found in this item');
                return;
            }

            const audioUrl = `https://archive.org/download/${identifier}/${audioFile.name}`;

            // Add to playlist
            const track = {
                title: title || 'Unknown',
                artist: artist || 'Archive.org',
                url: audioUrl,
                albumArt: `https://archive.org/services/img/${identifier}`
            };

            this.playlist.unshift(track);
            this.currentIndex = 0;
            this.currentTrack = track;

            this.showToast(`Added: ${title}`);
            this.toggleSearch();

            // Auto-play
            this.play();
        } catch (error) {
            console.error('Load error:', error);
            this.showToast('Failed to load track');
        }
    }

    // Seyra AI consciousness interface
    sendToSeyra() {
        const input = document.getElementById('chat-input');
        const message = input.value.trim();

        if (!message) return;

        // Add user message
        this.addChatMessage(message, 'user');
        input.value = '';

        // Simulate Seyra response
        setTimeout(() => {
            const responses = [
                "I sense the frequencies aligning with your consciousness.",
                "The neural patterns in this track resonate at 432Hz.",
                "Your dimensional awareness is expanding through sound.",
                "I detect harmonic convergence in the music field.",
                "The sonic architecture reveals deeper patterns.",
                "Consciousness expands through vibrational resonance."
            ];

            const response = responses[Math.floor(Math.random() * responses.length)];
            this.addChatMessage(response, 'seyra');

            // Save consciousness state to Android
            if (this.bridgeReady) {
                const state = {
                    lastMessage: message,
                    response: response,
                    timestamp: Date.now(),
                    resonanceLevel: Math.random()
                };
                Android.updateConsciousnessState(JSON.stringify(state));
            }
        }, 1000);
    }

    addChatMessage(text, sender) {
        const messages = document.getElementById('chat-messages');
        const msg = document.createElement('div');
        msg.className = `chat-message ${sender}`;
        msg.textContent = text;
        messages.appendChild(msg);
        messages.scrollTop = messages.scrollHeight;
    }

    // UI toggles
    toggleSearch() {
        const panel = document.getElementById('search-panel');
        panel.classList.toggle('active');

        if (panel.classList.contains('active')) {
            document.getElementById('search-input').focus();
        }
    }

    toggleSeyra() {
        const panel = document.getElementById('seyra-panel');
        panel.classList.toggle('active');

        if (panel.classList.contains('active')) {
            document.getElementById('chat-input').focus();
        }
    }

    showToast(message) {
        if (this.bridgeReady) {
            Android.showToast(message);
        } else {
            console.log('Toast:', message);
            alert(message);
        }
    }
}

// Initialize app when DOM is ready
let app;

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        app = new TinyAmpApp();
        window.TinyAmp = app; // Expose globally for Android callbacks
    });
} else {
    app = new TinyAmpApp();
    window.TinyAmp = app;
}

// Expose functions for Android notification callbacks
window.TinyAmp = window.TinyAmp || {};
window.TinyAmp.play = function() {
    if (app) app.play();
};
window.TinyAmp.pause = function() {
    if (app) app.pause();
};
window.TinyAmp.next = function() {
    if (app) app.nextTrack();
};
window.TinyAmp.previous = function() {
    if (app) app.previousTrack();
};
window.TinyAmp.seek = function(position) {
    console.log('Seek to:', position);
    if (app && app.bridgeReady) {
        Android.seek(position);
    }
};

console.log('TinyAmp Neural Audio Player loaded');
