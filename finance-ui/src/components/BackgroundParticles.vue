<template>
  <div class="starfield" aria-hidden="true">
    <canvas ref="canvasRef" class="starfield-canvas"></canvas>
    <div class="nebula nebula-1"></div>
    <div class="nebula nebula-2"></div>
    <div class="nebula nebula-3"></div>
    <div class="scan-line"></div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const canvasRef = ref(null)
let animationId = null
let stars = []
let shootingStars = []

const STAR_COUNT = 280
const SHOOTING_STAR_INTERVAL = 4000

class Star {
  constructor(canvas) {
    this.reset(canvas)
    this.y = Math.random() * canvas.height
  }

  reset(canvas) {
    this.x = Math.random() * canvas.width
    this.y = -2
    this.size = Math.random() * 1.8 + 0.3
    this.speed = Math.random() * 0.15 + 0.02
    this.opacity = Math.random() * 0.6 + 0.2
    this.twinkleSpeed = Math.random() * 0.02 + 0.005
    this.twinkleOffset = Math.random() * Math.PI * 2
  }

  update(canvas, time) {
    this.y += this.speed
    if (this.y > canvas.height + 2) this.reset(canvas)
    this.currentOpacity = this.opacity * (0.5 + 0.5 * Math.sin(time * this.twinkleSpeed + this.twinkleOffset))
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(180, 200, 255, ${this.currentOpacity})`
    ctx.fill()

    if (this.size > 1.2) {
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.size * 3, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(108, 124, 255, ${this.currentOpacity * 0.1})`
      ctx.fill()
    }
  }
}

class ShootingStar {
  constructor(canvas) {
    this.x = Math.random() * canvas.width * 0.7
    this.y = Math.random() * canvas.height * 0.4
    this.length = 80 + Math.random() * 120
    this.speed = 8 + Math.random() * 6
    this.angle = Math.PI / 4 + (Math.random() - 0.5) * 0.3
    this.opacity = 1
    this.decay = 0.015 + Math.random() * 0.01
    this.alive = true
  }

  update() {
    this.x += Math.cos(this.angle) * this.speed
    this.y += Math.sin(this.angle) * this.speed
    this.opacity -= this.decay
    if (this.opacity <= 0) this.alive = false
  }

  draw(ctx) {
    if (!this.alive) return
    const tailX = this.x - Math.cos(this.angle) * this.length
    const tailY = this.y - Math.sin(this.angle) * this.length

    const gradient = ctx.createLinearGradient(tailX, tailY, this.x, this.y)
    gradient.addColorStop(0, `rgba(108, 124, 255, 0)`)
    gradient.addColorStop(0.6, `rgba(180, 200, 255, ${this.opacity * 0.3})`)
    gradient.addColorStop(1, `rgba(255, 255, 255, ${this.opacity})`)

    ctx.beginPath()
    ctx.moveTo(tailX, tailY)
    ctx.lineTo(this.x, this.y)
    ctx.strokeStyle = gradient
    ctx.lineWidth = 1.5
    ctx.stroke()

    ctx.beginPath()
    ctx.arc(this.x, this.y, 2, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255, 255, 255, ${this.opacity})`
    ctx.fill()
  }
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  stars = Array.from({ length: STAR_COUNT }, () => {
    const s = new Star(canvas)
    s.y = Math.random() * canvas.height
    return s
  })

  let lastShootingTime = 0

  const animate = (time) => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    stars.forEach(star => {
      star.update(canvas, time)
      star.draw(ctx)
    })

    if (time - lastShootingTime > SHOOTING_STAR_INTERVAL + Math.random() * 3000) {
      shootingStars.push(new ShootingStar(canvas))
      lastShootingTime = time
    }

    shootingStars = shootingStars.filter(s => s.alive)
    shootingStars.forEach(s => {
      s.update()
      s.draw(ctx)
    })

    animationId = requestAnimationFrame(animate)
  }

  animationId = requestAnimationFrame(animate)

  onBeforeUnmount(() => {
    cancelAnimationFrame(animationId)
    window.removeEventListener('resize', resize)
  })
})
</script>

<style scoped>
.starfield {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.starfield-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.nebula {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0;
  animation: nebulaPulse 12s ease-in-out infinite;
}

.nebula-1 {
  width: 600px;
  height: 600px;
  top: -15%;
  left: -10%;
  background: radial-gradient(circle, rgba(108, 124, 255, 0.08), transparent 70%);
  animation-delay: 0s;
}

.nebula-2 {
  width: 500px;
  height: 500px;
  bottom: -10%;
  right: -5%;
  background: radial-gradient(circle, rgba(0, 232, 157, 0.06), transparent 70%);
  animation-delay: 4s;
}

.nebula-3 {
  width: 400px;
  height: 400px;
  top: 40%;
  left: 50%;
  background: radial-gradient(circle, rgba(255, 77, 106, 0.04), transparent 70%);
  animation-delay: 8s;
}

@keyframes nebulaPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.15); }
}

.scan-line {
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(108, 124, 255, 0.01) 2px,
    rgba(108, 124, 255, 0.01) 4px
  );
  pointer-events: none;
}

@media (prefers-reduced-motion: reduce) {
  .nebula {
    animation: none;
    opacity: 0.3;
  }
  .scan-line {
    display: none;
  }
}
</style>
