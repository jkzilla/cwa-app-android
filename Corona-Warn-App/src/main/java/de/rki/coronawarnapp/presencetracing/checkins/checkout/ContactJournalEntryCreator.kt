package de.rki.coronawarnapp.presencetracing.checkins.checkout

import dagger.Reusable
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Seconds
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToLong

@Reusable
class ContactJournalEntryCreator @Inject constructor(
    private val diaryRepository: ContactDiaryRepository
) {

    suspend fun createEntry(checkIn: CheckIn) {
        if (checkIn.createJournalEntry) {
            Timber.d("Creating journal entry for %s", this)

            // 1. Create location if missing
            val traceLocationIdString = checkIn.traceLocationId.utf8()
            val location: ContactDiaryLocation = diaryRepository.locations.first()
                .find { it.traceLocationID == traceLocationIdString } ?: checkIn.toLocation(traceLocationIdString)

            // 2. Split CheckIn by Midnight UTC
            val splitCheckIns = checkIn.splitByMidnightUTC()
            Timber.d("Split %s into %s ", this, splitCheckIns)

            // 3. Create LocationVisit
            splitCheckIns
                .map { it.toLocationVisit(location) }
                .forEach { diaryRepository.addLocationVisit(it) }
        }
    }

    private suspend fun CheckIn.toLocation(traceLocationIdString: String): ContactDiaryLocation {
        val location = DefaultContactDiaryLocation(
            locationName = locationName(),
            traceLocationID = traceLocationIdString
        )
        Timber.d("Created new location %s and adding it to contact journal db", location)
        diaryRepository.addLocation(location)

        // Get location from db cause we need the id autogenerated by db
        return diaryRepository.locations.first().first { it.traceLocationID == traceLocationIdString }
    }

    private fun CheckIn.locationName(): String {
        val nameParts = mutableListOf(description, address)

        if (traceLocationStart != null && traceLocationEnd != null) {
            if (traceLocationStart.millis > 0 && traceLocationEnd.millis > 0) {
                nameParts.add("$traceLocationStart - $traceLocationEnd")
            }
        }

        return nameParts.joinToString(separator = ", ")
    }

    private fun CheckIn.toLocationVisit(location: ContactDiaryLocation): ContactDiaryLocationVisit {
        // Use Seconds for more precision
        val durationInMinutes = Seconds.secondsBetween(checkInStart, checkInEnd).seconds / 60.0
        val duration = (durationInMinutes / 15).roundToLong() * 15
        return DefaultContactDiaryLocationVisit(
            date = checkInStart.toLocalDateUtc(),
            contactDiaryLocation = location,
            duration = Duration.standardMinutes(duration),
            checkInID = id
        )
    }
}
