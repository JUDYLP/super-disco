<template>
  <section class="receipt-root">
    <div class="receipt-stage" ref="stageRef">
      <div ref="canvasHostRef" class="canvas-host"></div>
      <div class="stage-overlay">
        <p class="stage-title">PERSONAL FINANCE</p>
        <p class="stage-sub">Monthly Statement</p>
      </div>
      <div class="stage-hint" ref="hintRef">
        <span class="hint-chip">Drag to interact</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import gsap from 'gsap'
import * as THREE from 'three'

const props = defineProps({
  billData: { type: Array, default: () => [] },
  dashboard: {
    type: Object,
    default: () => ({ month_income: 0, month_expense: 0, month_balance: 0 })
  },
  savingsRate: { type: Number, default: 0 },
  categoryStats: { type: Array, default: () => [] },
})

/* ═══════════════════════════════════════════════════════════════
   SPRING SYSTEM — 全局弹簧（旋转/位置/缩放/视差层）
   ═══════════════════════════════════════════════════════════════ */
class Spring {
  constructor(stiffness = 0.08, damping = 0.28) {
    this.stiffness = stiffness; this.damping = damping
    this.value = 0; this.velocity = 0; this.target = 0
  }
  update(dt) {
    const force = (this.target - this.value) * this.stiffness
    this.velocity += force; this.velocity *= (1 - this.damping)
    this.value += this.velocity * dt; return this.value
  }
  snap(v) { this.value = v; this.target = v; this.velocity = 0 }
}

class SpringVec3 {
  constructor(stiffness = 0.08, damping = 0.28) {
    this.x = new Spring(stiffness, damping)
    this.y = new Spring(stiffness, damping)
    this.z = new Spring(stiffness, damping)
  }
  setTarget(x, y, z) { this.x.target = x; this.y.target = y; this.z.target = z }
  update(dt) { return { x: this.x.update(dt), y: this.y.update(dt), z: this.z.update(dt) } }
  snap(x, y, z) { this.x.snap(x); this.y.snap(y); this.z.snap(z) }
}

class InertiaTracker {
  constructor(smoothing = 0.25) {
    this.vx = 0; this.vy = 0; this._sx = 0; this._sy = 0; this._st = 0; this.smoothing = smoothing
  }
  sample(x, y) {
    const now = performance.now(); const dt = now - this._st
    if (dt < 4) return
    const a = this.smoothing
    this.vx = this.vx * (1 - a) + (x - this._sx) / (dt * 0.001) * a
    this.vy = this.vy * (1 - a) + (y - this._sy) / (dt * 0.001) * a
    this._sx = x; this._sy = y; this._st = now
  }
  reset() { this.vx = 0; this.vy = 0 }
  decay(rate = 0.92) { this.vx *= rate; this.vy *= rate }
}

/* ═══════════════════════════════════════════════════════════════
   CLOTH PHYSICS — Verlet 积分 + 弹簧约束
   ═══════════════════════════════════════════════════════════════ */

class ClothParticle {
  constructor(x, y, z, pinned = false) {
    this.x = x; this.y = y; this.z = z
    this.ox = x; this.oy = y; this.oz = z  // 上一帧位置 (Verlet)
    this.ix = x; this.iy = y; this.iz = z  // 初始位置
    this.pinned = pinned
    this.pinStrength = pinned ? 1.0 : 0.0  // 0=自由, 1=完全固定
    this.mass = 1.0
  }
}

class ClothConstraint {
  constructor(p1, p2, stiffness = 1.0) {
    this.p1 = p1; this.p2 = p2; this.stiffness = stiffness
    const dx = p2.x - p1.x, dy = p2.y - p1.y, dz = p2.z - p1.z
    this.restLength = Math.sqrt(dx * dx + dy * dy + dz * dz)
  }
}

class ClothSimulation {
  constructor(width, height, segW, segH) {
    this.width = width; this.height = height
    this.segW = segW; this.segH = segH
    this.particles = []
    this.constraints = []
    this.gravity = -0.008
    this.damping = 0.985
    this.constraintIterations = 8

    /* 创建粒子网格 */
    const stepX = width / segW, stepY = height / segH
    const hw = width / 2, hh = height / 2

    for (let j = 0; j <= segH; j++) {
      for (let i = 0; i <= segW; i++) {
        const x = -hw + i * stepX
        const y = hh - j * stepY
        const z = 0

        /* 顶部两行部分约束 — 模拟被夹住 */
        let pinStrength = 0
        if (j === 0) pinStrength = 0.85        // 顶行强约束
        else if (j === 1) pinStrength = 0.35    // 第二行弱约束

        const p = new ClothParticle(x, y, z, pinStrength > 0)
        p.pinStrength = pinStrength
        this.particles.push(p)
      }
    }

    /* 结构约束 — 水平+垂直相邻 */
    for (let j = 0; j <= segH; j++) {
      for (let i = 0; i <= segW; i++) {
        const idx = j * (segW + 1) + i
        // 水平
        if (i < segW) {
          this.constraints.push(new ClothConstraint(
            this.particles[idx], this.particles[idx + 1], 0.9
          ))
        }
        // 垂直
        if (j < segH) {
          this.constraints.push(new ClothConstraint(
            this.particles[idx], this.particles[idx + segW + 1], 0.9
          ))
        }
      }
    }

    /* 剪切约束 — 对角线 */
    for (let j = 0; j < segH; j++) {
      for (let i = 0; i < segW; i++) {
        const idx = j * (segW + 1) + i
        this.constraints.push(new ClothConstraint(
          this.particles[idx], this.particles[idx + segW + 2], 0.6
        ))
        this.constraints.push(new ClothConstraint(
          this.particles[idx + 1], this.particles[idx + segW + 1], 0.6
        ))
      }
    }

    /* 弯曲约束 — 隔一个 */
    for (let j = 0; j <= segH; j++) {
      for (let i = 0; i + 2 <= segW; i++) {
        const idx = j * (segW + 1) + i
        this.constraints.push(new ClothConstraint(
          this.particles[idx], this.particles[idx + 2], 0.3
        ))
      }
    }
    for (let j = 0; j + 2 <= segH; j++) {
      for (let i = 0; i <= segW; i++) {
        const idx = j * (segW + 1) + i
        this.constraints.push(new ClothConstraint(
          this.particles[idx], this.particles[idx + 2 * (segW + 1)], 0.3
        ))
      }
    }
  }

  /* 对拖拽点施加力 — 温和传播 */
  applyForce(hitX, hitY, forceX, forceY, forceZ, radius = 0.5) {
    const rSq = radius * radius
    for (const p of this.particles) {
      const dx = p.x - hitX, dy = p.y - hitY
      const distSq = dx * dx + dy * dy
      if (distSq > rSq * 3) continue
      const influence = Math.exp(-distSq / (rSq * 0.6))
      if (influence < 0.002) continue

      const yNorm = (p.iy + this.height / 2) / this.height
      const freedom = 1.0 - p.pinStrength * (1.0 - yNorm * 0.5)

      p.x += forceX * influence * freedom
      p.y += forceY * influence * freedom
      p.z += forceZ * influence * freedom
    }
  }

  /* Verlet 积分步骤 */
  integrate(dt) {
    const dtSq = dt * dt
    const damp = this.damping
    const g = this.gravity

    for (const p of this.particles) {
      if (p.pinStrength >= 0.99) continue

      const vx = (p.x - p.ox) * damp
      const vy = (p.y - p.oy) * damp
      const vz = (p.z - p.oz) * damp

      p.ox = p.x; p.oy = p.y; p.oz = p.z

      /* 重力 — 底部更强 */
      const yNorm = (p.iy + this.height / 2) / this.height
      const gravScale = 0.3 + yNorm * 0.7

      p.x += vx
      p.y += vy + g * dtSq * gravScale * p.mass
      p.z += vz
    }
  }

  /* 约束求解 — 多次迭代 */
  solveConstraints() {
    for (let iter = 0; iter < this.constraintIterations; iter++) {
      for (const c of this.constraints) {
        const dx = c.p2.x - c.p1.x
        const dy = c.p2.y - c.p1.y
        const dz = c.p2.z - c.p1.z
        const dist = Math.sqrt(dx * dx + dy * dy + dz * dz)
        if (dist < 0.0001) continue

        const diff = (dist - c.restLength) / dist * c.stiffness * 0.5
        const ox = dx * diff, oy = dy * diff, oz = dz * diff

        const p1Free = 1.0 - c.p1.pinStrength
        const p2Free = 1.0 - c.p2.pinStrength
        const totalFree = p1Free + p2Free
        if (totalFree < 0.001) continue

        const r1 = p1Free / totalFree, r2 = p2Free / totalFree

        c.p1.x += ox * r1; c.p1.y += oy * r1; c.p1.z += oz * r1
        c.p2.x -= ox * r2; c.p2.y -= oy * r2; c.p2.z -= oz * r2
      }
    }
  }

  /* 部分约束 — 顶部粒子向初始位置恢复 */
  applyPinConstraint() {
    for (const p of this.particles) {
      if (p.pinStrength <= 0) continue
      const s = p.pinStrength * 0.15
      p.x += (p.ix - p.x) * s
      p.y += (p.iy - p.y) * s
      p.z += (p.iz - p.z) * s
    }
  }

  /* 呼吸动画 — 极微弱漂浮 */
  applyBreathing(time) {
    for (const p of this.particles) {
      const yNorm = (p.iy + this.height / 2) / this.height
      const xNorm = (p.ix + this.width / 2) / this.width
      const edgeX = Math.abs(xNorm - 0.5) * 2
      const edgeY = Math.abs(yNorm - 0.5) * 2
      const cornerW = Math.min(1, Math.sqrt(edgeX * edgeX + edgeY * edgeY))

      const p1 = time * 0.6 + p.ix * 0.5 + p.iy * 0.3
      const p2 = time * 0.9 + p.ix * 0.2 - p.iy * 0.5
      const wave = Math.sin(p1) * 0.12 + Math.sin(p2) * 0.08

      const breathZ = wave * cornerW * 0.004
      p.z += breathZ * (1.0 - p.pinStrength) * 0.2
    }
  }

  /* 回到初始形状的力 */
  applyRestForce(strength = 0.008) {
    for (const p of this.particles) {
      if (p.pinStrength >= 0.99) continue
      const s = strength * (1.0 - p.pinStrength * 0.5)
      p.x += (p.ix - p.x) * s
      p.y += (p.iy - p.y) * s
      p.z += (p.iz - p.z) * s
    }
  }

  /* 同步粒子位置到 Three.js geometry */
  syncToGeometry(geometry) {
    const pos = geometry.attributes.position
    const arr = pos.array
    for (let i = 0; i < this.particles.length; i++) {
      const p = this.particles[i]
      arr[i * 3] = p.x
      arr[i * 3 + 1] = p.y
      arr[i * 3 + 2] = p.z
    }
    pos.needsUpdate = true
    geometry.computeVertexNormals()
  }

  /* 完整模拟步骤 */
  step(dt, breathTime, isDragging) {
    this.integrate(dt)
    this.solveConstraints()
    this.applyPinConstraint()
    if (!isDragging) {
      this.applyBreathing(breathTime)
      this.applyRestForce(0.006)
    } else {
      this.applyRestForce(0.002)
    }
  }
}

/* ═══════════════════════════════════════════════════════════════
   CONSTANTS
   ═══════════════════════════════════════════════════════════════ */
const PAPER_W = 3.0
const PAPER_H = 5.4

function getSegments() {
  const cores = navigator.hardwareConcurrency || 2
  const mem = navigator.deviceMemory || 4
  if (cores <= 2 || mem <= 2) return { w: 20, h: 36 }
  if (cores <= 4 || mem <= 4) return { w: 32, h: 56 }
  return { w: 44, h: 76 }
}
const SEG = getSegments()

/* ═══════════════════════════════════════════════════════════════
   REFS & STATE
   ═══════════════════════════════════════════════════════════════ */
const stageRef = ref(null)
const canvasHostRef = ref(null)
const hintRef = ref(null)

let scene, camera, renderer, mesh, geometry, material, texture, raycaster
let shadowFloor, shadowFloorMat
let cloth = null
let animId = 0, isVisible = true, isDragging = false, isHovering = false
let resizeObs, visObs

const paperRotX = new Spring(0.035, 0.22)
const paperRotY = new Spring(0.035, 0.22)
const paperRotZ = new Spring(0.025, 0.30)
const paperPosX = new Spring(0.04, 0.20)
const paperPosY = new Spring(0.04, 0.20)
const paperPosZ = new Spring(0.06, 0.18)
const shadowPos = new SpringVec3(0.018, 0.32)
const highlightRot = new Spring(0.055, 0.16)
const ambientRot = new Spring(0.012, 0.35)
const scaleSpring = new Spring(0.07, 0.22)

const inertia = new InertiaTracker(0.22)
const pointer = { x: 0, y: 0, sx: 0, sy: 0 }
let mouseVec, prevLocalHit, curLocalHit

let breathTime = 0
let lastTime = 0
let dragVelX = 0, dragVelY = 0
let lastDragX = 0, lastDragY = 0
let dragSampleTime = 0

/* ═══════════════════════════════════════════════════════════════
   RECEIPT TEXTURE — 真实财务数据（优化字体大小）
   ═══════════════════════════════════════════════════════════════ */
const texCanvas = document.createElement('canvas')
const texCtx = texCanvas.getContext('2d')
texCanvas.width = 720
texCanvas.height = 1800

const fmt = (n) => {
  const abs = Math.abs(n)
  const s = abs.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  return (n < 0 ? '-' : '') + '¥' + s
}

const fmtShort = (n) => {
  const abs = Math.abs(n)
  if (abs >= 10000) return (n < 0 ? '-' : '') + '¥' + (abs / 10000).toFixed(1) + '万'
  return fmt(n)
}

const drawReceipt = () => {
  const ctx = texCtx
  const W = texCanvas.width
  const H = texCanvas.height
  const bills = props.billData || []
  const db = props.dashboard || { month_income: 0, month_expense: 0, month_balance: 0 }
  const sRate = props.savingsRate || 0
  const catStats = props.categoryStats || []

  /* 字体大小 — 大幅提升确保3D变形后仍可读 */
  const F_TITLE = 'bold 34px "SF Mono","JetBrains Mono",monospace'
  const F_SUBTITLE = '15px "SF Mono","JetBrains Mono",monospace'
  const F_SECTION = 'bold 15px "SF Mono","JetBrains Mono",monospace'
  const F_LABEL = 'bold 14px "SF Mono","JetBrains Mono",monospace'
  const F_VALUE = '14px "SF Mono","JetBrains Mono",monospace'
  const F_BODY = '14px "SF Mono","JetBrains Mono",monospace'
  const F_SMALL = '12px "SF Mono","JetBrains Mono",monospace'
  const F_TINY = '11px "SF Mono","JetBrains Mono",monospace'
  const F_BIG = 'bold 20px "SF Mono","JetBrains Mono",monospace'
  const F_HUGE = 'bold 24px "SF Mono","JetBrains Mono",monospace'

  ctx.fillStyle = '#FAFAF5'
  ctx.fillRect(0, 0, W, H)

  /* 纸张纤维噪点 */
  const imgData = ctx.getImageData(0, 0, W, H)
  const d = imgData.data
  for (let i = 0; i < d.length; i += 4) {
    const n = (Math.random() - 0.5) * 4
    d[i] += n; d[i+1] += n; d[i+2] += n
  }
  ctx.putImageData(imgData, 0, 0)

  /* 顶部撕裂线 */
  ctx.strokeStyle = 'rgba(0,0,0,0.12)'; ctx.setLineDash([2,4]); ctx.lineWidth = 0.5
  ctx.beginPath(); ctx.moveTo(0, 14); ctx.lineTo(W, 14); ctx.stroke()
  ctx.setLineDash([])

  /* ── 品牌区 ── */
  ctx.textAlign = 'center'; ctx.fillStyle = '#0A0A0A'
  ctx.font = F_TITLE
  ctx.fillText('PERSONAL FINANCE', W / 2, 68)
  ctx.font = F_SUBTITLE; ctx.fillStyle = '#999'
  ctx.fillText('*** MONTHLY STATEMENT ***', W / 2, 92)

  /* ── 日期 ── */
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const dateStr = `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())}`
  const timeStr = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  ctx.textAlign = 'left'; ctx.font = F_SMALL; ctx.fillStyle = '#666'
  ctx.fillText(`Date: ${dateStr}  ${timeStr}`, 30, 118)
  ctx.fillText(`Bills: ${bills.length} records`, 30, 138)

  /* ── KPI 概览区 ── */
  let cy = 164
  ctx.strokeStyle = '#999'; ctx.setLineDash([3,3]); ctx.lineWidth = 0.5
  ctx.beginPath(); ctx.moveTo(24, cy); ctx.lineTo(W-24, cy); ctx.stroke()
  ctx.setLineDash([])
  cy += 24

  ctx.font = F_SECTION; ctx.fillStyle = '#888'
  ctx.textAlign = 'center'
  ctx.fillText('MONTHLY OVERVIEW', W / 2, cy)
  cy += 26

  /* 收入 */
  ctx.fillStyle = '#0A8A5C'; ctx.font = F_LABEL
  ctx.textAlign = 'left'; ctx.fillText('INCOME', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText(fmt(db.month_income), W - 30, cy)
  cy += 24

  /* 支出 */
  ctx.fillStyle = '#CC3D55'; ctx.font = F_LABEL
  ctx.textAlign = 'left'; ctx.fillText('EXPENSE', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText(fmt(db.month_expense), W - 30, cy)
  cy += 24

  /* 余额 */
  ctx.fillStyle = db.month_balance >= 0 ? '#0A0A0A' : '#CC3D55'
  ctx.font = F_BIG
  ctx.textAlign = 'left'; ctx.fillText('BALANCE', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText(fmt(db.month_balance), W - 30, cy)
  ctx.textAlign = 'left'
  cy += 26

  /* 储蓄率 */
  ctx.fillStyle = '#555'; ctx.font = F_VALUE
  ctx.textAlign = 'left'; ctx.fillText('SAVINGS RATE', 30, cy)
  ctx.textAlign = 'right'
  const srColor = sRate >= 30 ? '#0A8A5C' : sRate >= 10 ? '#B8860B' : '#CC3D55'
  ctx.fillStyle = srColor
  ctx.fillText(sRate + '%', W - 30, cy)
  cy += 8

  /* 储蓄率进度条 */
  cy += 10
  const barX = 30, barW = W - 60, barH = 10
  ctx.fillStyle = 'rgba(0,0,0,0.06)'
  ctx.beginPath(); ctx.roundRect(barX, cy, barW, barH, 5); ctx.fill()
  const fillW = Math.min(Math.abs(sRate), 100) / 100 * barW
  ctx.fillStyle = srColor
  ctx.beginPath(); ctx.roundRect(barX, cy, Math.max(8, fillW), barH, 5); ctx.fill()
  cy += 26

  /* ── 分类统计 ── */
  if (catStats.length > 0) {
    ctx.strokeStyle = '#999'; ctx.setLineDash([3,3]); ctx.lineWidth = 0.4
    ctx.beginPath(); ctx.moveTo(24, cy); ctx.lineTo(W-24, cy); ctx.stroke()
    ctx.setLineDash([])
    cy += 22
    ctx.font = F_SECTION; ctx.fillStyle = '#888'
    ctx.textAlign = 'center'; ctx.fillText('EXPENSE BY CATEGORY', W / 2, cy)
    cy += 24

    const catColors = ['#6c7cff','#00e89d','#ffb347','#ff4d6a','#06B6D4','#8B5CF6','#10B981','#F97316']
    const maxCat = Math.max(...catStats.map(c => c.value || 0), 1)

    catStats.slice(0, 6).forEach((cat, i) => {
      const val = cat.value || 0
      const pct = (val / maxCat) * 100

      ctx.fillStyle = catColors[i % catColors.length]
      ctx.fillRect(30, cy - 10, 10, 10)

      ctx.fillStyle = '#1A1A1A'; ctx.font = F_BODY
      ctx.textAlign = 'left'; ctx.fillText(cat.name || 'Other', 46, cy)
      ctx.textAlign = 'right'; ctx.fillText(fmtShort(val), W - 30, cy)

      cy += 8
      const miniBarW = W - 60
      ctx.fillStyle = 'rgba(0,0,0,0.04)'
      ctx.fillRect(30, cy, miniBarW, 5)
      ctx.fillStyle = catColors[i % catColors.length] + '60'
      ctx.fillRect(30, cy, miniBarW * pct / 100, 5)
      cy += 18
    })
  }

  /* ── 账单明细 ── */
  cy += 8
  ctx.strokeStyle = '#555'; ctx.setLineDash([5,3]); ctx.lineWidth = 0.5
  ctx.beginPath(); ctx.moveTo(24, cy); ctx.lineTo(W-24, cy); ctx.stroke()
  ctx.setLineDash([])
  cy += 22

  ctx.font = F_SECTION; ctx.fillStyle = '#888'
  ctx.textAlign = 'center'; ctx.fillText('TRANSACTION DETAILS', W / 2, cy)
  cy += 22

  /* 列头 */
  ctx.font = F_LABEL; ctx.fillStyle = '#0A0A0A'
  ctx.textAlign = 'left'; ctx.fillText('ITEM', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText('AMOUNT', W - 30, cy)
  ctx.textAlign = 'left'
  ctx.strokeStyle = '#BBB'; ctx.lineWidth = 0.4
  ctx.beginPath(); ctx.moveTo(24, cy + 6); ctx.lineTo(W-24, cy + 6); ctx.stroke()
  cy += 24

  /* 最多显示 10 条 */
  const displayBills = bills.slice(0, 10)
  ctx.font = F_BODY

  displayBills.forEach((bill, i) => {
    if (cy > H - 180) return

    if (i % 2 === 0) {
      ctx.fillStyle = 'rgba(0,0,0,0.015)'
      ctx.fillRect(24, cy - 14, W - 48, 26)
    }

    const isIncome = bill.type === 'income'
    const name = (bill.name || '').length > 16 ? (bill.name || '').slice(0, 14) + '..' : (bill.name || '')
    const amount = (isIncome ? '+' : '-') + fmt(Math.abs(bill.amount || 0))

    ctx.fillStyle = '#1A1A1A'; ctx.textAlign = 'left'; ctx.fillText(name, 30, cy)
    ctx.fillStyle = isIncome ? '#0A8A5C' : '#CC3D55'
    ctx.textAlign = 'right'; ctx.fillText(amount, W - 30, cy)
    ctx.textAlign = 'left'

    /* 日期小字 */
    const date = (bill.consume_date || bill.date || '').slice(5, 10)
    if (date) {
      cy += 14
      ctx.fillStyle = '#AAA'; ctx.font = F_TINY
      ctx.fillText(date, 34, cy)
      ctx.font = F_BODY
    }
    cy += 22
  })

  if (bills.length > 10) {
    cy += 6
    ctx.fillStyle = '#999'; ctx.font = F_SMALL
    ctx.textAlign = 'center'
    ctx.fillText(`... and ${bills.length - 10} more records`, W / 2, cy)
    ctx.textAlign = 'left'
    cy += 22
  }
  /* ── 合计区 ── */
  cy += 8
  ctx.strokeStyle = '#555'; ctx.setLineDash([5,3]); ctx.lineWidth = 0.5
  ctx.beginPath(); ctx.moveTo(24, cy); ctx.lineTo(W-24, cy); ctx.stroke()
  ctx.setLineDash([])
  cy += 26

  ctx.fillStyle = '#0A0A0A'; ctx.font = F_HUGE
  ctx.textAlign = 'left'; ctx.fillText('TOTAL', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText(fmt(db.month_expense + db.month_income), W - 30, cy)
  ctx.textAlign = 'left'

  cy += 28
  ctx.font = F_VALUE; ctx.fillStyle = '#777'
  ctx.fillText('Net Income', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText(fmt(db.month_balance), W - 30, cy); ctx.textAlign = 'left'

  cy += 22
  ctx.font = F_BIG
  ctx.fillStyle = db.month_balance >= 0 ? '#0A8A5C' : '#CC3D55'
  ctx.fillText('NET BALANCE', 30, cy)
  ctx.textAlign = 'right'; ctx.fillText(fmt(db.month_balance), W - 30, cy); ctx.textAlign = 'left'

  /* ── 条形码 ── */
  cy += 38
  ctx.strokeStyle = '#999'; ctx.setLineDash([3,3]); ctx.lineWidth = 0.4
  ctx.beginPath(); ctx.moveTo(24, cy); ctx.lineTo(W-24, cy); ctx.stroke()
  ctx.setLineDash([])
  cy += 22
  ctx.fillStyle = '#0A0A0A'
  for (let i = 0; i < 50; i++) {
    const bw = i % 7 === 0 ? 3 : i % 4 === 0 ? 1.5 : 1
    ctx.fillRect(80 + i * 9, cy, bw, 30)
  }
  cy += 40
  ctx.textAlign = 'center'
  ctx.font = F_TINY; ctx.fillStyle = '#555'
  ctx.fillText(`PF-${dateStr.replace(/-/g, '')}`, W / 2, cy)
  cy += 18
  ctx.font = 'bold 14px "SF Mono","JetBrains Mono",monospace'; ctx.fillStyle = '#0A0A0A'
  ctx.fillText('THANK YOU FOR TRACKING', W / 2, cy)

  /* 底部撕裂线 */
  cy += 22
  ctx.strokeStyle = 'rgba(0,0,0,0.12)'; ctx.setLineDash([2,4]); ctx.lineWidth = 0.5
  ctx.beginPath(); ctx.moveTo(0, cy); ctx.lineTo(W, cy); ctx.stroke()
  ctx.setLineDash([])

  if (texture) texture.needsUpdate = true
}

/* ═══════════════════════════════════════════════════════════════
   SCENE SETUP
   ═══════════════════════════════════════════════════════════════ */
const setupScene = () => {
  const T = THREE
  scene = new T.Scene()
  scene.background = new T.Color('#F8F7F2')
  scene.fog = new T.FogExp2('#F8F7F2', 0.035)

  camera = new T.PerspectiveCamera(28, 1, 0.1, 50)
  camera.position.set(0, 0.2, 10)
  camera.lookAt(0, 0, 0)

  const dpr = Math.min(window.devicePixelRatio, 2)
  renderer = new T.WebGLRenderer({ antialias: true, alpha: true, powerPreference: 'high-performance' })
  renderer.setPixelRatio(dpr)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = T.PCFSoftShadowMap
  renderer.outputColorSpace = T.SRGBColorSpace
  renderer.toneMapping = T.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.15
  canvasHostRef.value.appendChild(renderer.domElement)

  texture = new T.CanvasTexture(texCanvas)
  texture.colorSpace = T.SRGBColorSpace
  texture.anisotropy = renderer.capabilities.getMaxAnisotropy()

  geometry = new T.PlaneGeometry(PAPER_W, PAPER_H, SEG.w, SEG.h)
  material = new T.MeshStandardMaterial({
    map: texture, side: T.DoubleSide,
    roughness: 0.88, metalness: 0.0,
    color: new T.Color('#FEFEFB'),
  })
  mesh = new T.Mesh(geometry, material)
  mesh.castShadow = true; mesh.receiveShadow = true
  mesh.position.y = 0.1
  scene.add(mesh)

  /* ── 布料物理 ── */
  cloth = new ClothSimulation(PAPER_W, PAPER_H, SEG.w, SEG.h)
  /* 初始弯曲 — 让纸张有自然弧度和微弱重力下垂 */
  for (const p of cloth.particles) {
    const yNorm = (p.iy + PAPER_H / 2) / PAPER_H  // 0=顶, 1=底
    const xNorm = (p.ix + PAPER_W / 2) / PAPER_W   // 0=左, 1=右

    /* 自然下垂 — 底部微弱 */
    const sag = -yNorm * yNorm * 0.02

    /* 微弱横向弯曲 */
    const curl = Math.sin(yNorm * Math.PI) * 0.015

    /* 边缘微翘 */
    const edgeDist = Math.abs(xNorm - 0.5) * 2
    const edgeCurl = edgeDist > 0.7 ? (edgeDist - 0.7) * yNorm * 0.01 : 0

    /* 微弱随机扰动 */
    const noise = (Math.sin(p.ix * 7.3 + p.iy * 13.7) * 0.002)

    p.z = curl + sag + edgeCurl + noise
    p.oz = p.z
    p.iz = p.z
  }
  cloth.syncToGeometry(geometry)

  /* 光照 */
  scene.add(new T.AmbientLight('#FDFBF7', 1.8))
  scene.add(new T.HemisphereLight('#FFFFFF', '#E8E0D4', 0.8))

  const key = new T.DirectionalLight('#FFFEF8', 4.5)
  key.position.set(6, 8, 10); key.castShadow = true
  key.shadow.mapSize.set(2048, 2048)
  key.shadow.camera.near = 0.5; key.shadow.camera.far = 30
  key.shadow.camera.left = -6; key.shadow.camera.right = 6
  key.shadow.camera.top = 8; key.shadow.camera.bottom = -8
  key.shadow.bias = -0.00008; key.shadow.normalBias = 0.02; key.shadow.radius = 3
  scene.add(key)

  const fill = new T.DirectionalLight('#E8F0FF', 1.5); fill.position.set(-5, 2, -3); scene.add(fill)
  const rim = new T.DirectionalLight('#FFFFFF', 2.0); rim.position.set(0, -3, -10); scene.add(rim)
  const spot = new T.PointLight('#FFF8E8', 1.5, 20, 2); spot.position.set(0, 6, 4); scene.add(spot)

  const floorGeo = new T.PlaneGeometry(14, 14)
  shadowFloorMat = new T.ShadowMaterial({ opacity: 0.12 })
  shadowFloor = new T.Mesh(floorGeo, shadowFloorMat)
  shadowFloor.rotation.x = -Math.PI / 2; shadowFloor.position.set(0, -3.2, 0)
  shadowFloor.receiveShadow = true; scene.add(shadowFloor)

  raycaster = new T.Raycaster()
  mouseVec = new T.Vector2()
  prevLocalHit = new T.Vector3()
  curLocalHit = new T.Vector3()

  updateRendererSize()
  drawReceipt()

  /* 入场动画 */
  mesh.scale.set(0.5, 0.5, 0.5)
  mesh.rotation.set(-0.06, 0.12, 0.08)
  mesh.position.set(0.3, 5, -2)
  paperRotX.snap(-0.06); paperRotY.snap(0.12); paperRotZ.snap(0.08)
  paperPosX.snap(0.3); paperPosY.snap(5); paperPosZ.snap(-2)
  scaleSpring.snap(0.5)
  paperRotX.target = 0; paperRotY.target = 0; paperRotZ.target = 0
  paperPosX.target = 0; paperPosY.target = 0.1; paperPosZ.target = 0
  scaleSpring.target = 1.0
  shadowPos.snap(0, -3.2, 0)
}

/* ═══════════════════════════════════════════════════════════════
   SCREEN → LOCAL HIT
   ═══════════════════════════════════════════════════════════════ */
const screenToLocal = (e) => {
  if (!renderer || !mesh) return null
  const rect = renderer.domElement.getBoundingClientRect()
  pointer.x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  pointer.y = -((e.clientY - rect.top) / rect.height) * 2 + 1
  mouseVec.set(pointer.x, pointer.y)
  raycaster.setFromCamera(mouseVec, camera)
  const hits = raycaster.intersectObject(mesh, false)
  return hits.length > 0 ? mesh.worldToLocal(hits[0].point.clone()) : null
}

/* ═══════════════════════════════════════════════════════════════
   EVENTS — 拖拽交互（通过布料物理传播力）
   ═══════════════════════════════════════════════════════════════ */
const onPointerDown = (e) => {
  const hit = screenToLocal(e)
  if (!hit) return
  isDragging = true
  prevLocalHit.copy(hit)
  dragVelX = 0; dragVelY = 0
  lastDragX = hit.x; lastDragY = hit.y
  dragSampleTime = performance.now()
  inertia.reset()
  paperPosZ.target = 0.3; scaleSpring.target = 1.03
  renderer.domElement.style.cursor = 'grabbing'
}

const onPointerMove = (e) => {
  if (!renderer || !mesh) return
  const hit = screenToLocal(e)
  if (!isDragging) {
    isHovering = !!hit
    renderer.domElement.style.cursor = hit ? 'grab' : 'default'
    scaleSpring.target = hit ? 1.01 : 1.0
    return
  }
  if (!hit || !cloth) return
  curLocalHit.copy(hit)
  const dx = curLocalHit.x - prevLocalHit.x
  const dy = curLocalHit.y - prevLocalHit.y
  const dragMag = Math.sqrt(dx * dx + dy * dy)

  /* 布料微弱变形 — 只在拖拽点附近 */
  cloth.applyForce(hit.x, hit.y, dx * 0.2, dy * 0.2, dragMag * 0.01 + 0.003, 0.5)

  /* 整体轻微跟随鼠标 — 有边界限制 */
  paperPosX.target = Math.max(-1.5, Math.min(1.5, paperPosX.target + dx * 0.18))
  paperPosY.target = Math.max(-1.5, Math.min(1.8, paperPosY.target + dy * 0.18))

  /* 微弱旋转 */
  paperRotY.target = Math.max(-0.3, Math.min(0.3, paperRotY.target + dx * 0.05))
  paperRotX.target = Math.max(-0.25, Math.min(0.25, paperRotX.target - dy * 0.03))

  prevLocalHit.copy(curLocalHit)
}

const onPointerUp = () => {
  if (!isDragging) return
  isDragging = false
  scaleSpring.target = isHovering ? 1.02 : 1.0
  renderer.domElement.style.cursor = isHovering ? 'grab' : 'default'
  /* 释放后平滑回到原位 */
  paperPosZ.target = 0; paperRotX.target = 0; paperRotY.target = 0; paperRotZ.target = 0
  paperPosX.target = 0; paperPosY.target = 0.1
}

/* ═══════════════════════════════════════════════════════════════
   RENDER LOOP
   ═══════════════════════════════════════════════════════════════ */
const renderLoop = (ts) => {
  animId = window.requestAnimationFrame(renderLoop)
  if (!renderer || !scene || !camera || !mesh || !isVisible) return

  const dt = lastTime ? Math.min((ts - lastTime) / 1000, 0.05) : 0.016
  lastTime = ts
  const t = ts * 0.001

  /* ── 布料物理步骤 ── */
  if (cloth) {
    breathTime += dt * 0.28
    cloth.step(dt, breathTime, isDragging)
    cloth.syncToGeometry(geometry)
  }

  /* ── 平滑鼠标指针 ── */
  pointer.sx += (pointer.x - pointer.sx) * 0.025
  pointer.sy += (pointer.y - pointer.sy) * 0.025

  /* ── 3D空间漂浮（idle） — 极微弱 ── */
  if (!isDragging) {
    paperPosX.target = Math.sin(t * 0.18) * 0.006
    paperPosY.target = 0.1 + Math.sin(t * 0.24 + 1.3) * 0.010
    paperPosZ.target = Math.sin(t * 0.12 + 2.7) * 0.004
    paperRotX.target = -pointer.sy * 0.05
    paperRotY.target = pointer.sx * 0.08
    paperRotZ.target = Math.sin(t * 0.14) * 0.004
  }

  /* ── 更新所有弹簧 ── */
  const rx = paperRotX.update(dt), ry = paperRotY.update(dt), rz = paperRotZ.update(dt)
  const px = paperPosX.update(dt), py = paperPosY.update(dt), pz = paperPosZ.update(dt)
  const sc = scaleSpring.update(dt)

  mesh.rotation.set(rx, ry, rz)
  mesh.position.set(px, py, pz)
  mesh.scale.set(sc, sc, sc)

  /* ── 阴影延迟跟随 ── */
  shadowPos.setTarget(px * 0.6, -3.2, pz * 0.3 + 0.5)
  const sp = shadowPos.update(dt)
  shadowFloor.position.set(sp.x, sp.y, sp.z)
  shadowFloorMat.opacity = 0.08 + Math.max(0, Math.min(1, (py + 3.2) / 3.5)) * 0.14

  /* ── 高光偏移 ── */
  highlightRot.target = ry * 1.3
  const hlRot = highlightRot.update(dt)
  scene.children.forEach(child => {
    if (child.isDirectionalLight && child.castShadow) {
      child.position.x = 6 + hlRot * 2; child.position.z = 10 + hlRot * 1.5
    }
  })

  /* ── 环境光延迟 ── */
  ambientRot.target = ry * 0.5
  const ambRot = ambientRot.update(dt)
  scene.children.forEach(child => {
    if (child.isHemisphereLight) child.position.x = ambRot * 3
  })

  if (!isDragging) inertia.decay(0.94)
  renderer.render(scene, camera)
}

/* ═══════════════════════════════════════════════════════════════
   SIZE
   ═══════════════════════════════════════════════════════════════ */
const updateRendererSize = () => {
  if (!renderer || !canvasHostRef.value) return
  const w = canvasHostRef.value.clientWidth, h = canvasHostRef.value.clientHeight
  if (!w || !h) return
  renderer.setSize(w, h, false)
  camera.aspect = w / h; camera.updateProjectionMatrix()
}

/* ═══════════════════════════════════════════════════════════════
   LIFECYCLE
   ═══════════════════════════════════════════════════════════════ */
const bindEvents = () => {
  if (!renderer) return
  renderer.domElement.addEventListener('pointerdown', onPointerDown)
  renderer.domElement.addEventListener('pointermove', onPointerMove)
  window.addEventListener('pointerup', onPointerUp)
  window.addEventListener('pointercancel', onPointerUp)
  renderer.domElement.addEventListener('touchstart', (e) => e.preventDefault(), { passive: false })
}

const unbindEvents = () => {
  if (!renderer) return
  renderer.domElement.removeEventListener('pointerdown', onPointerDown)
  renderer.domElement.removeEventListener('pointermove', onPointerMove)
  window.removeEventListener('pointerup', onPointerUp)
  window.removeEventListener('pointercancel', onPointerUp)
}

const destroy = () => {
  if (animId) window.cancelAnimationFrame(animId)
  animId = 0; unbindEvents()
  resizeObs?.disconnect(); visObs?.disconnect()
  texture?.dispose(); material?.dispose(); geometry?.dispose()
  renderer?.dispose(); renderer?.domElement?.remove()
  scene = camera = renderer = mesh = geometry = material = texture = raycaster = null
  cloth = null
  mouseVec = prevLocalHit = curLocalHit = null
}

/* 数据变化时重绘纹理 */
watch(() => [props.billData, props.dashboard, props.savingsRate, props.categoryStats], () => {
  drawReceipt()
}, { deep: true })

onMounted(() => {
  if (!canvasHostRef.value) return
  setupScene(); bindEvents()
  resizeObs = new ResizeObserver(() => updateRendererSize())
  resizeObs.observe(canvasHostRef.value)
  visObs = new IntersectionObserver(
    ([e]) => { isVisible = Boolean(e?.isIntersecting) },
    { threshold: 0.05 }
  )
  visObs.observe(stageRef.value)
  lastTime = performance.now()
  animId = window.requestAnimationFrame(renderLoop)
  gsap.fromTo(hintRef.value, { opacity: 0, y: 12 }, { opacity: 1, y: 0, duration: 1.0, delay: 1.2, ease: 'power2.out' })
})

onBeforeUnmount(() => destroy())
</script>

<style scoped>
.receipt-root { width: 100%; }

.receipt-stage {
  position: relative; width: 100%;
  aspect-ratio: 16 / 10; min-height: 480px;
  border-radius: 20px; background: #F8F7F2;
  overflow: hidden; touch-action: none;
  -webkit-user-select: none; user-select: none;
  box-shadow:
    0 0 0 1px rgba(0,0,0,0.04),
    0 4px 16px rgba(0,0,0,0.03),
    0 16px 48px rgba(0,0,0,0.04);
}

.canvas-host { width: 100%; height: 100%; }
.canvas-host :deep(canvas) {
  display: block; width: 100% !important; height: 100% !important; touch-action: none;
}

.stage-overlay { position: absolute; top: 28px; left: 32px; pointer-events: none; z-index: 2; }
.stage-title {
  margin: 0; font-family: 'JetBrains Mono','SF Mono',monospace;
  font-size: 11px; font-weight: 600; letter-spacing: 0.18em;
  text-transform: uppercase; color: rgba(0,0,0,0.25);
}
.stage-sub {
  margin: 4px 0 0; font-family: 'JetBrains Mono','SF Mono',monospace;
  font-size: 10px; color: rgba(0,0,0,0.15); letter-spacing: 0.06em;
}

.stage-hint { position: absolute; bottom: 24px; left: 50%; transform: translateX(-50%); pointer-events: none; z-index: 2; }
.hint-chip {
  display: inline-flex; align-items: center; height: 28px; padding: 0 16px;
  border-radius: 999px; background: rgba(255,255,255,0.7);
  backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(0,0,0,0.05); color: rgba(0,0,0,0.35);
  font-family: 'JetBrains Mono','SF Mono',monospace;
  font-size: 10px; letter-spacing: 0.08em; text-transform: uppercase;
}

@media (max-width: 900px) { .receipt-stage { aspect-ratio: 4/3; min-height: 380px; } }
@media (max-width: 560px) {
  .receipt-stage { aspect-ratio: 3/4; min-height: 320px; border-radius: 14px; }
  .stage-overlay { top: 18px; left: 20px; }
}
</style>
