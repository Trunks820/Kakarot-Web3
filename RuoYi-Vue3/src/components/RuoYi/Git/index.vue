<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('https://github.com/Trunks820/Kakarot-Web3')

function goto() {
  window.open(url.value)
}
</script>
