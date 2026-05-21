const FALLBACK_IMAGES = [
  'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=800&h=500&fit=crop',
  'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=800&h=500&fit=crop',
]

function getFallbackImage(id, title) {
  let hash = 0
  const seed = title || String(id)
  for (let i = 0; i < seed.length; i++) {
    hash = ((hash << 5) - hash + seed.charCodeAt(i)) | 0
  }
  return FALLBACK_IMAGES[Math.abs(hash) % FALLBACK_IMAGES.length]
}

export { FALLBACK_IMAGES, getFallbackImage }
