import { nextTick } from 'vue'

let buttons = []

export function useCodeCopy() {
  function injectCopyButtons() {
    nextTick(() => {
      buttons.forEach(b => b.remove())
      buttons = []

      document.querySelectorAll('.markdown-body pre').forEach(pre => {
        if (pre.parentElement?.classList.contains('code-block-wrapper')) return

        const wrapper = document.createElement('div')
        wrapper.className = 'code-block-wrapper'
        pre.parentNode.insertBefore(wrapper, pre)
        wrapper.appendChild(pre)

        const button = document.createElement('button')
        button.className = 'copy-btn'
        button.textContent = '复制'
        button.addEventListener('click', () => copyCode(pre, button))
        wrapper.appendChild(button)
        buttons.push(button)
      })
    })
  }

  return { injectCopyButtons }
}

async function copyCode(pre, button) {
  const code = pre.querySelector('code')
  const text = code ? code.textContent : pre.textContent
  try {
    await navigator.clipboard.writeText(text)
    button.textContent = '已复制!'
    button.classList.add('copied')
    setTimeout(() => {
      button.textContent = '复制'
      button.classList.remove('copied')
    }, 2000)
  } catch {
    button.textContent = '失败'
    setTimeout(() => { button.textContent = '复制' }, 2000)
  }
}
