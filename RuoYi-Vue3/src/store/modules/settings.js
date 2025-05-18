import defaultSettings from '@/settings'
import { useDark, useToggle } from '@vueuse/core'
import { useDynamicTitle } from '@/utils/dynamicTitle'

const storageSetting = JSON.parse(localStorage.getItem('layout-setting') || '{}')

// Initialize dark mode based on storage or system preference
const isDark = useDark({
  storageKey: 'theme-preference',
  valueDark: 'dark',
  valueLight: '',
  initialValue: storageSetting.isDark === true || window.matchMedia('(prefers-color-scheme: dark)').matches,
  onChanged: (dark) => {
    // Ensure the class is directly set on document element
    if (dark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }
})
const toggleDark = useToggle(isDark)

const { sideTheme, showSettings, topNav, tagsView, fixedHeader, sidebarLogo, dynamicTitle } = defaultSettings

const useSettingsStore = defineStore(
  'settings',
  {
    state: () => ({
      title: '',
      theme: storageSetting.theme || '#409EFF',
      sideTheme: storageSetting.sideTheme || sideTheme,
      showSettings: showSettings,
      topNav: storageSetting.topNav === undefined ? topNav : storageSetting.topNav,
      tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
      fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
      sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
      dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle,
      isDark: isDark.value
    }),
    actions: {
      // Change layout settings
      changeSetting(data) {
        const { key, value } = data
        if (this.hasOwnProperty(key)) {
          this[key] = value
        }
      },
      // Set webpage title
      setTitle(title) {
        this.title = title
        useDynamicTitle()
      },
      // Toggle dark mode
      toggleTheme() {
        this.isDark = !this.isDark
        
        // Directly set the class on html element
        if (this.isDark) {
          document.documentElement.classList.add('dark')
        } else {
          document.documentElement.classList.remove('dark')
        }
        
        // Call toggle from VueUse (might not be needed since we're setting directly)
        toggleDark()
        
        // Save theme preference
        const layoutSetting = JSON.parse(localStorage.getItem('layout-setting') || '{}')
        layoutSetting.isDark = this.isDark
        localStorage.setItem('layout-setting', JSON.stringify(layoutSetting))
        
        // Also set theme-preference
        localStorage.setItem('theme-preference', this.isDark ? 'dark' : '')

        // Force reflow
        document.body.offsetHeight
      }
    }
  })

// Ensure dark mode is properly set on initialization
if (isDark.value) {
  document.documentElement.classList.add('dark')
} else {
  document.documentElement.classList.remove('dark')
}

export default useSettingsStore
