package de.rki.coronawarnapp.ui.onboarding

import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class OnboardingNotificationsFragmentTest : BaseUITest() {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @Before
    fun setup() {
        setupMockViewModel(object : OnboardingNotificationsViewModel.Factory {
            override fun create(): OnboardingNotificationsViewModel = OnboardingNotificationsViewModel()
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingNotificationsFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingNotificationsFragment>()
        // Check any view to make sure screenshot is not blank
        onView(withId(R.id.onboarding_button_next)).check(matches(isDisplayed()))
        Screengrab.screenshot(OnboardingNotificationsFragment::class.simpleName)
    }
}

@Module
abstract class OnboardingNotificationsTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingNotificationsFragment(): OnboardingNotificationsFragment
}
