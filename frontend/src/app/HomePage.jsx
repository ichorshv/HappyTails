import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import Modal from '../shared/ui/Modal'
import heroPets from '../assets/home/hero-pets.png'
import circle1 from '../assets/home/why/circle-1.svg'
import circle2 from '../assets/home/why/circle-2.svg'
import circle3 from '../assets/home/why/circle-3.svg'
import circle4 from '../assets/home/why/circle-4.svg'
import heart from '../assets/home/why/heart.svg'
import house from '../assets/home/why/house.svg'
import pawToe1 from '../assets/home/why/paw-toe-1.svg'
import pawToe2 from '../assets/home/why/paw-toe-2.svg'
import pawToe3 from '../assets/home/why/paw-toe-3.svg'
import pawToe4 from '../assets/home/why/paw-toe-4.svg'
import pawPad from '../assets/home/why/paw-pad.svg'
import treeCrown from '../assets/home/why/tree-crown.svg'
import treeTrunk from '../assets/home/why/tree-trunk.svg'
import treeBranchLeft from '../assets/home/why/tree-branch-left.svg'
import treeBranchRight from '../assets/home/why/tree-branch-right.svg'

/**
 * Main call-to-action: green gradient with a soft colored glow.
 */
const CTA =
  'flex items-center justify-center rounded-pill bg-linear-to-r from-leaf to-teal font-sans font-bold whitespace-nowrap text-white shadow-[0_6px_18px_-8px_color-mix(in_oklab,var(--color-teal)_60%,transparent)] transition duration-200 hover:-translate-y-px hover:shadow-[0_10px_24px_-8px_color-mix(in_oklab,var(--color-teal)_70%,transparent)] focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-leaf'

/** Frosted glass surface shared by the hero, the cards and the closing band. */
const GLASS = 'border border-white/70 bg-white/45 shadow-soft backdrop-blur-2xl'

const RESPONSIBILITIES = [
  {
    title: 'Това е ангажимент за години',
    text: 'Кучетата и котките живеят 10–15 години, а често и повече. Осиновяването не е решение за сезон, а обещание за целия живот на животното. Много от тях вече са били изоставени веднъж и второ връщане в приюта им тежи още повече.',
    points: [
      'Помисли къде ще живееш след няколко години и дали животното ще може да дойде с теб.',
      'Ако живееш под наем, провери дали хазяинът позволява домашни любимци.',
      'Реши кой ще се грижи за него, когато пътуваш или си болен.',
    ],
  },
  {
    title: 'Нужни са време и внимание',
    text: 'Животното има нужда от теб всеки ден, не само когато ти е удобно. Кучетата се нуждаят от разходки поне два пъти дневно, а котките от игра и компания. Затова във въпросника питаме колко часа ще остава само.',
    points: [
      'Първите седмици са най-трудни: новият дом е непознат и стресиращ.',
      'Обучението изисква търпение и постоянство, а не наказания.',
      'Животно, което е само по цял ден, често става тревожно или разрушително.',
    ],
  },
  {
    title: 'Грижата струва пари',
    text: 'Освен храна има ваксини, обезпаразитяване, редовни прегледи и неочаквани разходи при болест или инцидент. Планирай ги предварително, за да не се налага животното да се връща в приюта заради пари.',
    points: [
      'Качествената храна е основата на здравето и често спестява разходи за ветеринар.',
      'Заложи месечна сума за непредвидени прегледи и лечение.',
      'Помисли и за нашийник, повод, легло, играчки и транспортна чанта.',
    ],
  },
  {
    title: 'Подготви дома си',
    text: 'Новото място е непознато и плашещо. Преди животното да пристигне, направи дома безопасен и приготви всичко нужно, за да се почувства спокойно от първия ден. Всяко животно в каталога има и изисквания към дома, прочети ги внимателно.',
    points: [
      'Прибери кабели, препарати, лекарства и растения, които са отровни за животни.',
      'Осигури му тихо място за почивка, където никой няма да го безпокои.',
      'Ако имаш деца или други животни, ги запознавай постепенно и под наблюдение.',
    ],
  },
]

/** "Защо да осиновите?" cards. Icon parts are positioned inside the 250px circle from Figma. */
const REASONS = [
  {
    title: 'Даряваш нов живот',
    circle: circle1,
    icon: [{ src: heart, width: 193.5, height: 182.5, x: 26.75, y: 42.75 }],
    text: 'Ти му даваш дом. То ти дава цялото си сърце. За много от животните в приюта това ще е първият истински дом – място, където са в безопасност и обичани.',
    footer: [
      ['Топло място, което', 'да нарече свое'],
      ['Семейство,', 'на което да се довери'],
    ],
  },
  {
    title: 'Някой винаги те чака',
    circle: circle2,
    icon: [{ src: house, width: 162.5, height: 192.5, x: 44.75, y: 19.75 }],
    text: 'Всеки ден у дома ще те посреща някой, който просто се радва, че си там. Без значение какъв ден си имал - опашката ще маха, а мъркането ще те успокои.',
    footer: [
      ['Компания в дългите', 'вечери'],
      ['Повод за', 'разходка всеки ден'],
    ],
  },
  {
    title: 'Правиш място за още един',
    circle: circle3,
    icon: [
      { src: pawToe1, width: 40.5, height: 46.5, x: 70.72, y: 32.72 },
      { src: pawToe2, width: 40.5, height: 46.5, x: 138.73, y: 32.72, flip: true },
      { src: pawToe3, width: 40.5, height: 45.5, x: 26.75, y: 88.7 },
      { src: pawToe4, width: 40.5, height: 45.5, x: 182.75, y: 88.7, flip: true },
      { src: pawPad, width: 138.5, height: 113.5, x: 55.75, y: 98.71 },
    ],
    text: 'Всяко осиновяване освобождава място в приюта за следващото животно в нужда. Така с едно решение помагаш не на едно, а на две животни.',
    footer: [
      ['Едно животно', 'намира своя дом'],
      ['Друго получава', 'място и грижа'],
    ],
  },
  {
    title: 'Опознавате се спокойно',
    circle: circle4,
    icon: [
      { src: treeCrown, width: 157.5, height: 150.5, x: 49.75, y: 26.79 },
      { src: treeTrunk, width: 2.5, height: 137.5, x: 126.71, y: 77.8 },
      { src: treeBranchLeft, width: 26.5, height: 25.5, x: 102.73, y: 100.81 },
      { src: treeBranchRight, width: 27.5, height: 25.5, x: 126.71, y: 128.8 },
    ],
    text: 'Първо излизате заедно на разходка, после решавате - без бързане. Разходката е в приюта и трае около час, за да усетите дали си пасвате.',
    footer: [
      ['Запознанство', 'в позната за него среда'],
      ['Решението е общо -', 'твое и на приюта'],
    ],
  },
]

/** Short version of scenarios С1–С5 for the "Как става осиновяването?" window. */
const ADOPTION_STEPS = [
  'Разгледай животните и избери това, което ти допада.',
  'Подай заявление и попълни кратък въпросник за дома си.',
  'Ако всичко е наред, влизаш в опашката за животното и получаваш имейл с мястото си.',
  'Когато дойде твоят ред, имаш 48 часа да запазиш час за разходка.',
  'След разходката служителите на приюта одобряват заявлението.',
  'Вземаш новия си приятел у дома.',
]

const CARD_STICKY_TOP = 96

/**
 * Cards of one section have the same height and stick at the same spot, so each next card
 * lands exactly on top of the previous one.
 *
 * The cards are glass, so the part of a card that is under the next one is cut away (clip-path):
 * nothing shows through, and a fully covered card disappears. Everything is computed from the
 * scroll position, so scrolling back up brings the cards back the same way.
 */
function useStackedCards() {
  const cards = useRef([])

  useEffect(() => {
    // Same breakpoint as md:sticky on the cards; on phones the cards are a normal list
    const stacking = window.matchMedia('(min-width: 768px)')

    let frame = 0
    const update = () => {
      frame = 0
      const list = cards.current.filter(Boolean)
      if (!stacking.matches) {
        list.forEach((card) => {
          card.style.clipPath = ''
          card.style.visibility = ''
        })
        return
      }
      list.forEach((card, i) => {
        const next = list[i + 1]
        if (!next) return
        const height = card.offsetHeight
        const top = card.getBoundingClientRect().top
        const nextTop = next.getBoundingClientRect().top

        // Height of the part still above the next card
        const visible = nextTop - top
        card.style.visibility = visible <= 0 ? 'hidden' : 'visible'
        card.style.clipPath = visible >= height ? '' : `inset(0 0 ${height - Math.max(visible, 0)}px 0)`
      })
    }
    const onScroll = () => {
      if (!frame) frame = requestAnimationFrame(update)
    }

    update()
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onScroll)
    return () => {
      cancelAnimationFrame(frame)
      window.removeEventListener('scroll', onScroll)
      window.removeEventListener('resize', onScroll)
    }
  }, [])

  return (i) => (el) => {
    cards.current[i] = el
  }
}

/** Figma icon (circle + line drawing) drawn at 250px and scaled down to `size`. */
function ReasonIcon({ circle, icon, size = 112 }) {
  return (
    <div aria-hidden="true" className="relative shrink-0" style={{ width: size, height: size }}>
      <div className="absolute top-0 left-0 size-[250px] origin-top-left" style={{ transform: `scale(${size / 250})` }}>
        <img src={circle} alt="" width="250" height="250" className="absolute inset-0" />
        {icon.map(({ src, width, height, x, y, flip }) => (
          <img
            key={`${x}-${y}`}
            src={src}
            alt=""
            width={width}
            height={height}
            style={{ left: x, top: y }}
            className={`absolute max-w-none ${flip ? '-scale-x-100' : ''}`}
          />
        ))}
      </div>
    </div>
  )
}

/** Cards stick below the header from md up; the next one slides over (see useStackedCards). */
const STACK_CARD = 'md:sticky md:top-(--sticky-top) md:transform-gpu md:justify-center'
const STACK_STYLE = { '--sticky-top': `${CARD_STICKY_TOP}px` }

function ReasonCard({ title, circle, icon, text, footer, cardRef }) {
  return (
    <article ref={cardRef} style={STACK_STYLE} className={`flex w-full flex-col gap-6 rounded-[36px] p-8 sm:p-12 ${GLASS} ${STACK_CARD} md:h-[440px]`}>
      <div className="flex items-center gap-5">
        <ReasonIcon circle={circle} icon={icon} size={112} />
        <h3 className="text-[26px] leading-tight font-bold sm:text-[34px]">{title}</h3>
      </div>
      <p className="text-lg leading-relaxed opacity-85 sm:text-[21px]">{text}</p>
      <div className="grid grid-cols-[1fr_auto_1fr] gap-4 border-t border-white/80 pt-6 text-center text-base leading-snug font-semibold sm:text-lg">
        <p>
          {footer[0][0]} {footer[0][1]}
        </p>
        <div aria-hidden="true" className="w-px bg-deep/20" />
        <p>
          {footer[1][0]} {footer[1][1]}
        </p>
      </div>
    </article>
  )
}

/** Heading on the left that stays in place, cards on the right that stack while scrolling. */
function CardSection({ id, eyebrow, title, children }) {
  return (
    <section id={id} className="grid scroll-mt-24 gap-8 lg:grid-cols-[1fr_760px] lg:gap-10">
      <div className="lg:sticky lg:top-[96px] lg:self-start lg:pt-10">
        <p className="text-lg font-semibold text-peach sm:text-xl">{eyebrow}</p>
        <h2 className="mt-2 text-3xl leading-tight font-bold text-balance sm:text-[40px]">{title}</h2>
      </div>
      <div className="flex flex-col gap-6 md:gap-10">{children}</div>
    </section>
  )
}

/** Home page: hero, "Защо да осиновите?" and responsibility cards, all on frosted glass. */
export default function HomePage() {
  const reasonRef = useStackedCards()
  const responsibilityRef = useStackedCards()
  const [stepsOpen, setStepsOpen] = useState(false)

  // The hero fills the first screen; bottom padding gives the last card room to scroll up onto the stack
  return (
    <div className="flex flex-col gap-16 pb-16 md:gap-24 md:pb-[max(4rem,calc(100svh-96px-560px-256px))]">
      <section
        className="grid min-h-[calc(100svh-120px)] items-center justify-items-center gap-6 text-center lg:grid-cols-2 lg:gap-0"
      >
        <div className="flex flex-col items-center">
          <h1 className="text-6xl leading-none font-bold whitespace-nowrap sm:text-8xl lg:text-[112px]">Дай шанс.</h1>
          <p className="mt-5 max-w-[700px] text-2xl leading-snug sm:text-[34px]">
            Всяко животно тук има история. <br className="hidden sm:block" />
            Нека следващата глава бъде с теб.
          </p>
          {/* The link sits centered right under the button */}
          <div className="mt-8 flex w-[320px] flex-col items-center gap-3 lg:w-[360px]">
            {/* /animals is the catalog page from #24 (not merged yet) */}
            <Link
              to="/animals"
              className={`${CTA} w-full py-5 text-2xl`}
            >
              Запознай се с тях
            </Link>
            <button
              type="button"
              onClick={() => setStepsOpen(true)}
              className="cursor-pointer font-sans text-xl whitespace-nowrap text-deep"
            >
              Как става осиновяването?
            </button>
          </div>
        </div>
        <img
          src={heroPets}
          alt="Куче и котка, сгушени едно до друго"
          width="1778"
          height="1184"
          className="w-full max-w-[560px] lg:w-[125%] lg:max-w-none"
        />
      </section>

      <CardSection id="why" eyebrow="Защо да осиновите?" title="Само малко любов променя два живота.">
        {REASONS.map((reason, i) => (
          <ReasonCard key={reason.title} {...reason} cardRef={reasonRef(i)} />
        ))}
      </CardSection>

      <CardSection id="responsibility" eyebrow="Преди да решиш" title="Осиновяването е отговорност.">
        {RESPONSIBILITIES.map(({ title, text, points }, i) => (
          <article
            key={title}
            ref={responsibilityRef(i)}
            style={STACK_STYLE}
            className={`flex w-full flex-col gap-5 rounded-[36px] p-8 sm:p-12 ${GLASS} ${STACK_CARD} md:h-[560px]`}
          >
            <h3 className="text-[26px] leading-tight font-bold sm:text-[34px]">{title}</h3>
            <p className="text-lg leading-relaxed opacity-85 sm:text-[21px]">{text}</p>
            <ul className="flex flex-col gap-3 text-base leading-relaxed opacity-85 sm:text-lg">
              {points.map((point) => (
                <li key={point} className="flex gap-3">
                  <span aria-hidden="true" className="mt-[0.6em] size-2 shrink-0 rounded-full bg-leaf" />
                  {point}
                </li>
              ))}
            </ul>
          </article>
        ))}
      </CardSection>

      <section
        className={`flex flex-col items-center gap-6 rounded-[40px] px-6 py-10 text-center sm:px-10 md:flex-row md:justify-between md:text-left ${GLASS}`}
      >
        <div>
          <h2 className="text-3xl leading-tight font-bold sm:text-[36px]">Готов ли си за нов приятел?</h2>
          <p className="mt-2 text-lg opacity-85">Разгледай животните, които чакат своя дом.</p>
        </div>
        <Link
          to="/animals"
          className={`${CTA} shrink-0 px-10 py-4 text-xl`}
        >
          Запознай се с тях
        </Link>
      </section>

      <Modal open={stepsOpen} onClose={() => setStepsOpen(false)} title="Как става осиновяването?">
        <ol className="flex list-decimal flex-col gap-3 pl-6 text-lg leading-relaxed">
          {ADOPTION_STEPS.map((step) => (
            <li key={step}>{step}</li>
          ))}
        </ol>
      </Modal>
    </div>
  )
}
