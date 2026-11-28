import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import apiClient, { toFormError } from '../../shared/apiClient'
import Button from '../../shared/ui/Button'

const dayFormat = new Intl.DateTimeFormat('bg-BG', {
    weekday: 'short',
    day: 'numeric',
    month: 'long',
})
const timeFormat = new Intl.DateTimeFormat('bg-BG', { hour: '2-digit', minute: '2-digit' })

function dayKey(iso) {
    const d = new Date(iso)
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${d.getFullYear()}-${month}-${day}`
}

/** Calendar where an applicant books a walk: /walks/book?animalId=5&applicationId=12 */
export default function WalkCalendarPage() {
    const [params] = useSearchParams()
    const animalId = params.get('animalId')
    const applicationId = params.get('applicationId')
    const missingParams = !animalId || !applicationId

    const [slots, setSlots] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [reloadKey, setReloadKey] = useState(0)
    const [selectedDay, setSelectedDay] = useState(null)
    const [selectedSlot, setSelectedSlot] = useState(null)
    const [booking, setBooking] = useState(false)
    const [booked, setBooked] = useState(null)

    useEffect(() => {
        if (!animalId) {
            return undefined
        }
        let cancelled = false
        apiClient
            .get('/walks/slots', { params: { animalId } })
            .then((response) => {
                if (cancelled) return
                const data = Array.isArray(response.data) ? response.data : (response.data.slots ?? [])
                setSlots(data.map((slot) => (typeof slot === 'string' ? slot : slot.startsAt)))
            })
            .catch((err) => {
                if (!cancelled) {
                    setError(toFormError(err, 'Не успяхме да заредим свободните часове.').message)
                }
            })
            .finally(() => {
                if (!cancelled) setLoading(false)
            })
        return () => {
            cancelled = true
        }
    }, [animalId, reloadKey])

    const slotsByDay = useMemo(() => {
        const groups = new Map()
        for (const slot of slots) {
            const key = dayKey(slot)
            if (!groups.has(key)) groups.set(key, [])
            groups.get(key).push(slot)
        }
        return groups
    }, [slots])

    const days = [...slotsByDay.keys()]
    const activeDay = selectedDay && slotsByDay.has(selectedDay) ? selectedDay : days[0]
    const daySlots = activeDay ? slotsByDay.get(activeDay) : []

    async function handleBook() {
        setBooking(true)
        setError('')
        try {
            const response = await apiClient.post('/walks', {
                applicationId: Number(applicationId),
                startsAt: selectedSlot,
            })
            setBooked(response.data)
        } catch (err) {
            setError(toFormError(err, 'Не успяхме да запишем разходката. Опитайте отново.').message)
            setSelectedSlot(null)
            setReloadKey((key) => key + 1)
        } finally {
            setBooking(false)
        }
    }

    if (missingParams) {
        return (
            <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-page p-4 text-ink">
                <p>Липсват данни за кучето или заявката. Върнете се и опитайте отново.</p>
                <Link to="/" className="font-bold underline">
                    Начало
                </Link>
            </main>
        )
    }

    if (booked) {
        return (
            <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-page p-4 text-ink">
                <h1 className="text-3xl font-bold">Разходката е записана!</h1>
                <p className="text-xl">
                    {dayFormat.format(new Date(booked.startsAt))},{' '}
                    {timeFormat.format(new Date(booked.startsAt))}
                </p>
                <Link to="/" className="font-bold underline">
                    Начало
                </Link>
            </main>
        )
    }

    return (
        <main className="flex min-h-screen flex-col items-center gap-6 bg-page p-4 text-ink">
            <h1 className="text-3xl font-bold">Запиши разходка</h1>

            {error && (
                <p role="alert" className="rounded-pill bg-danger px-6 py-2 text-white">
                    {error}
                </p>
            )}

            {loading && <p>Зареждане...</p>}

            {!loading && days.length === 0 && <p>В момента няма свободни часове.</p>}

            {days.length > 0 && (
                <>
                    <div className="flex flex-wrap justify-center gap-2">
                        {days.map((day) => (
                            <button
                                key={day}
                                type="button"
                                onClick={() => {
                                    setSelectedDay(day)
                                    setSelectedSlot(null)
                                }}
                                className={`rounded-pill px-5 py-2 font-bold shadow-soft transition ${
                                    day === activeDay ? 'bg-mint' : 'bg-glass hover:bg-mint/70'
                                }`}
                            >
                                {dayFormat.format(new Date(slotsByDay.get(day)[0]))}
                            </button>
                        ))}
                    </div>

                    <div className="flex max-w-xl flex-wrap justify-center gap-2">
                        {daySlots.map((slot) => (
                            <button
                                key={slot}
                                type="button"
                                onClick={() => setSelectedSlot(slot)}
                                aria-pressed={slot === selectedSlot}
                                className={`rounded-pill px-5 py-2 shadow-soft transition ${
                                    slot === selectedSlot ? 'bg-leaf font-bold text-white' : 'bg-glass hover:bg-mint'
                                }`}
                            >
                                {timeFormat.format(new Date(slot))}
                            </button>
                        ))}
                    </div>

                    <Button onClick={handleBook} disabled={!selectedSlot || booking}>
                        {booking ? 'Записване...' : 'Потвърди'}
                    </Button>
                </>
            )}
        </main>
    )
}