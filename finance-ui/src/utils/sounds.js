/**
 * Sound effects module — synthesized via Web Audio API.
 * No external audio files needed.
 *
 * Performance optimizations:
 *  - Single AudioContext reused across all sounds
 *  - Noise buffer cached and reused
 *  - Oscillator nodes created per-tone (unavoidable) but gain nodes minimized
 *  - Reduced particle count on low-end devices detected via hardwareConcurrency
 */

let ctx = null
let noiseBuffer = null

function getCtx() {
  if (!ctx) {
    ctx = new (window.AudioContext || window.webkitAudioContext)()
  }
  if (ctx.state === 'suspended') ctx.resume()
  return ctx
}

/** Pre-generate a noise buffer once and reuse it */
function getNoiseBuffer() {
  if (noiseBuffer) return noiseBuffer
  const ac = getCtx()
  const duration = 0.15
  const bufferSize = Math.floor(ac.sampleRate * duration)
  noiseBuffer = ac.createBuffer(1, bufferSize, ac.sampleRate)
  const data = noiseBuffer.getChannelData(0)
  for (let i = 0; i < bufferSize; i++) {
    data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (bufferSize * 0.15))
  }
  return noiseBuffer
}

function playTone(freq, duration, type = 'sine', volume = 0.12, delay = 0) {
  const ac = getCtx()
  const t = ac.currentTime + delay
  const osc = ac.createOscillator()
  const gain = ac.createGain()
  osc.type = type
  osc.frequency.setValueAtTime(freq, t)
  gain.gain.setValueAtTime(volume, t)
  gain.gain.exponentialRampToValueAtTime(0.001, t + duration)
  osc.connect(gain)
  gain.connect(ac.destination)
  osc.start(t)
  osc.stop(t + duration)
}

function playNoise(duration, volume = 0.04, delay = 0) {
  const ac = getCtx()
  const t = ac.currentTime + delay
  const source = ac.createBufferSource()
  source.buffer = getNoiseBuffer()
  const gain = ac.createGain()
  gain.gain.setValueAtTime(volume, t)
  gain.gain.exponentialRampToValueAtTime(0.001, t + duration)
  const filter = ac.createBiquadFilter()
  filter.type = 'highpass'
  filter.frequency.setValueAtTime(3000, t)
  source.connect(filter)
  filter.connect(gain)
  gain.connect(ac.destination)
  source.start(t, 0, duration)
}

/**
 * Coin drop sound — income added.
 * Two metallic clinks with a shimmer.
 */
export function playCoinDrop() {
  playTone(2800, 0.08, 'sine', 0.10, 0)
  playTone(4200, 0.06, 'sine', 0.06, 0.03)
  playNoise(0.05, 0.03, 0.01)
  playTone(3200, 0.12, 'sine', 0.05, 0.06)
}

/**
 * Cash register "ka-ching" — income saved.
 */
export function playKaChing() {
  playTone(1200, 0.06, 'square', 0.06, 0)
  playTone(1800, 0.08, 'sine', 0.08, 0.05)
  playTone(2400, 0.10, 'sine', 0.05, 0.10)
  playNoise(0.03, 0.02, 0.02)
}

/**
 * Soft whoosh — expense removed / money out.
 */
export function playWhoosh() {
  const ac = getCtx()
  const t = ac.currentTime
  const osc = ac.createOscillator()
  const gain = ac.createGain()
  osc.type = 'sine'
  osc.frequency.setValueAtTime(800, t)
  osc.frequency.exponentialRampToValueAtTime(200, t + 0.25)
  gain.gain.setValueAtTime(0.08, t)
  gain.gain.exponentialRampToValueAtTime(0.001, t + 0.25)
  osc.connect(gain)
  gain.connect(ac.destination)
  osc.start(t)
  osc.stop(t + 0.25)
  playNoise(0.15, 0.02, 0.02)
}

/**
 * Delete / trash sound — bill removed.
 */
export function playDelete() {
  playTone(400, 0.10, 'sawtooth', 0.05, 0)
  playTone(250, 0.15, 'sawtooth', 0.04, 0.05)
  playNoise(0.12, 0.03, 0.03)
}

/**
 * Success chime — operation completed.
 */
export function playSuccess() {
  playTone(880, 0.10, 'sine', 0.08, 0)
  playTone(1100, 0.10, 'sine', 0.06, 0.08)
  playTone(1320, 0.15, 'sine', 0.07, 0.16)
}

/**
 * Error buzz — validation failed.
 */
export function playError() {
  playTone(200, 0.15, 'square', 0.06, 0)
  playTone(180, 0.15, 'square', 0.05, 0.12)
}

/**
 * Tick sound — small amount change / keystroke in amount field.
 */
export function playTick() {
  playTone(3600, 0.03, 'sine', 0.04, 0)
}

/**
 * Detect if device is likely low-performance.
 * Returns a number 1-3: 1 = low, 2 = medium, 3 = high
 */
export function getPerformanceTier() {
  const cores = navigator.hardwareConcurrency || 2
  const memory = navigator.deviceMemory || 4
  if (cores <= 2 || memory <= 2) return 1
  if (cores <= 4 || memory <= 4) return 2
  return 3
}

/**
 * Get recommended particle count based on device performance.
 */
export function getRecommendedParticleCount() {
  const tier = getPerformanceTier()
  if (tier === 1) return 3
  if (tier === 2) return 5
  return 6
}
