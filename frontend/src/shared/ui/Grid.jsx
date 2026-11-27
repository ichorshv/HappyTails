/**
 * Columns from the Figma layout grid: 4 columns with a 16px gap on phones, 8 columns with
 * a 24px gap from md (768px) and 12 columns from lg (1024px). Children choose their width
 * with col-span-*, e.g. "col-span-4 md:col-span-4 lg:col-span-3".
 */
export default function Grid({ as: Tag = 'div', className = '', children, ...props }) {
  return (
    <Tag className={`grid grid-cols-4 gap-4 md:grid-cols-8 md:gap-6 lg:grid-cols-12 ${className}`} {...props}>
      {children}
    </Tag>
  )
}
