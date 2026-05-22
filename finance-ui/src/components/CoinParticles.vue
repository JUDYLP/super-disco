<template>
  <canvas ref="canvasRef" class="coin-canvas" />
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const canvasRef = ref(null)
let animFrameId = null
let particles = []
let particleId = 0

const INCOME_COLOR = { r: 0, g: 232, b: 157 }
const EXPENSE_COLOR = { r: 255, g: 77, b: 106 }

class CoinParticle {
  constructor(id, direction, color, startX, startY, endX, endY, delay, size) {
    this.id = id
    this.direction = direction
    this.color = color
    this.startX = startX
    this.startY = startY
    this.endX = endX
    this.endY = endY
    this.delay = delay
    this.size = size
    this.born = performance.now() + delay
    this.duration = 600
    this.fadeDuration = 250
    this.alive = true
    this.rotation = (Math.random() - 0.5) * Math.PI * 4
    this.rotSpeed = (Math.random() - 0.5) * 0.02
  }

  update(now) {
    const elapsed = now - this.born
    if (elapsed < 0) return // still in delay
    const progress = Math.min(elapsed / this.duration, 1)
    // Ease out cubic
    const eased = 1 - Math.pow(1 - progress, 3)

    this.x = this.startX + (this.endX - this.startX) * eased
    this.y = this.startY + (this.endY - this.startY) * eased
    this.scale = this.direction === 'in'
      ? 0.3 + 0.7 * eased
      : 1 - 0.5 * eased
    this.rotation += this.rotSpeed

    if (progress >= 1) {
      const fadeElapsed = elapsed - this.duration
      const fadeProgress = Math.min(fadeElapsed / this.fadeDuration, 1)
      this.opacity = 1 - fadeProgress
      if (fadeProgress >= 1) this.alive = false
    } else {
      this.opacity = Math.min(progress * 3, 1)
    }
  }

  draw(ctx) {
    if (this.opacity <= 0) return
    const { r, g, b } = this.color
    const s = this.size

    ctx.save()
    ctx.translate(this.x, this.y)
    ctx.rotate(this.rotation)
    ctx.scale(this.scale, this.scale)
    ctx.globalAlpha = this.opacity

    // Outer ring
    ctx.beginPath()
    ctx.arc(0, 0, s * 0.45, 0, Math.PI * 2)
    ctx.strokeStyle = `rgba(${r},${g},${b},0.5)`
    ctx.lineWidth = 1.2
    ctx.stroke()

    // Inner fill
    ctx.beginPath()
    ctx.arc(0, 0, s * 0.28, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${r},${g},${b},0.2)`
    ctx.fill()

    // Dollar sign
    ctx.fillStyle = `rgba(${r},${g},${b},0.8)`
    ctx.font = `600 ${s * 0.38}px "JetBrains Mono", monospace`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('$', 0, 0.5)

    // Glow
    ctx.beginPath()
    ctx.arc(0, 0, s * 0.5, 0, Math.PI * 2)
    const grad = ctx.createRadialGradient(0, 0, 0, 0, 0, s * 0.5)
    grad.addColorStop(0, `rgba(${r},${g},${b},0.08)`)
    grad.addColorStop(1, `rgba(${r},${g},${b},0)`)
    ctx.fillStyle = grad
    ctx.fill()

    ctx.restore()
  }
}

function animate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const now = performance.now()

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  particles.forEach(p => {
    p.update(now)
    p.draw(ctx)
  })

  particles = particles.filter(p => p.alive)

  if (particles.length > 0) {
    animFrameId = requestAnimationFrame(animate)
  } else {
    animFrameId = null
  }
}

function startAnimation() {
  if (!animFrameId) {
    animFrameId = requestAnimationFrame(animate)
  }
}

function spawnCoins(direction, count = 5) {
  const canvas = canvasRef.value
  if (!canvas) return

  const color = direction === 'in' ? INCOME_COLOR : EXPENSE_COLOR
  const sizes = [6, 8, 10, 8, 6]

  for (let i = 0; i < count; i++) {
    const size = sizes[i % sizes.length]
    const startX = direction === 'in'
      ? Math.random() * canvas.width * 0.3
      : canvas.width * 0.4 + Math.random() * canvas.width * 0.3
    const startY = canvas.height * 0.5 + (Math.random() - 0.5) * 200
    const endX = direction === 'in'
      ? canvas.width * 0.08 + Math.random() * 80
      : canvas.width * 0.7 + Math.random() * 80
    const endY = 60 + Math.random() * 40

    const p = new CoinParticle(
      particleId++,
      direction,
      color,
      startX, startY,
      endX, endY,
      i * 50,
      size
    )
    particles.push(p)
  }

  startAnimation()
}

function spawnIncome(count) { spawnCoins('in', count) }
function spawnExpense(count) { spawnCoins('out', count) }

function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  canvas.width = window.innerWidth * dpr
  canvas.height = window.innerHeight * dpr
  canvas.style.width = window.innerWidth + 'px'
  canvas.style.height = window.innerHeight + 'px'
  const ctx = canvas.getContext('2d')
  ctx.scale(dpr, dpr)
}

onMounted(() => {
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)
})

onBeforeUnmount(() => {
  if (animFrameId) cancelAnimationFrame(animFrameId)
  window.removeEventListener('resize', resizeCanvas)
})

defineExpose({ spawnIncome, spawnExpense })
</script>

<style scoped>
.coin-canvas {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 9999;
}
</style>
