package org.jenkinsci.plugins.parameterizedscheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParameterizedCronTabListTest {
	private static final Locale defaultLocale = Locale.getDefault();
	@Mock
	private ParameterizedCronTab mockParameterizedCronTab;
	@Mock
	private ParameterizedCronTab mockParameterizedCronTabToo;

	@BeforeAll
	static void initLocale() {
		Locale.setDefault(Locale.ENGLISH);
	}

	@AfterAll
	static void resetLocale() {
		Locale.setDefault(defaultLocale);
	}

	@Test
	void create() {
		ParameterizedCronTabList testObject = ParameterizedCronTabList.create("* * * * *%foo=bar");
		assertTrue(testObject.checkSanity().startsWith("Do you really mean \"every minute\""), testObject.checkSanity());
		List<ParameterizedCronTab> actualCronTabs = testObject.check(new GregorianCalendar());
		assertThat(actualCronTabs.size(), is(1));
		assertEquals(Collections.singletonMap("foo", "bar"), actualCronTabs.get(0).getParameterValues());
	}

	@Test
	void createMultiple() {
		ParameterizedCronTabList testObject = ParameterizedCronTabList.create("* * * * *%foo=bar\n*/1 * * * *%bar=bar");
		assertTrue(testObject.checkSanity().startsWith("Do you really mean \"every minute\""), testObject.checkSanity());
		List<ParameterizedCronTab> actualCronTabs = testObject.check(new GregorianCalendar());
		assertThat(actualCronTabs.size(), is(2));
		assertEquals(Collections.singletonMap("foo", "bar"), actualCronTabs.get(0).getParameterValues());
		assertEquals(Collections.singletonMap("bar", "bar"), actualCronTabs.get(1).getParameterValues());
	}

	@Test
	void check_Delegates_ReturnsNull() {
		ParameterizedCronTabList testObject = new ParameterizedCronTabList(Arrays.asList(mockParameterizedCronTab,
				mockParameterizedCronTabToo));
		GregorianCalendar testCalendar = new GregorianCalendar();
		List<ParameterizedCronTab> tabList = testObject.check(testCalendar);
		assertThat(tabList, is(empty()));

		verify(mockParameterizedCronTab).check(testCalendar);
		verify(mockParameterizedCronTabToo).check(testCalendar);
	}

	@Test
	void check_Delegates_ReturnsSame() {
		ParameterizedCronTabList testObject = new ParameterizedCronTabList(Arrays.asList(mockParameterizedCronTab,
				mockParameterizedCronTabToo));
		GregorianCalendar testCalendar = new GregorianCalendar();

		when(mockParameterizedCronTab.check(testCalendar)).thenReturn(true);
		when(mockParameterizedCronTabToo.check(testCalendar)).thenReturn(false);
		List<ParameterizedCronTab> tabList = testObject.check(testCalendar);
		assertThat(tabList.size(), is(1));
		assertSame(mockParameterizedCronTab, tabList.get(0));
	}

	@Test
	void check_Delegates_ReturnsBoth() {
		ParameterizedCronTabList testObject = new ParameterizedCronTabList(Arrays.asList(mockParameterizedCronTab,
				mockParameterizedCronTabToo));
		GregorianCalendar testCalendar = new GregorianCalendar();

		when(mockParameterizedCronTab.check(testCalendar)).thenReturn(true);
		when(mockParameterizedCronTabToo.check(testCalendar)).thenReturn(true);
		List<ParameterizedCronTab> tabList = testObject.check(testCalendar);
		assertThat(tabList.size(), is(2));

		assertSame(mockParameterizedCronTab, tabList.get(0));
		assertSame(mockParameterizedCronTabToo, tabList.get(1));
	}

	@Test
	void checkSanity_Delegates_ReturnsNull() {
		ParameterizedCronTabList testObject = new ParameterizedCronTabList(Arrays.asList(mockParameterizedCronTab,
				mockParameterizedCronTabToo));

		assertNull(testObject.checkSanity());

		verify(mockParameterizedCronTab).checkSanity();
		verify(mockParameterizedCronTabToo).checkSanity();
	}

	@Test
	void checkSanity_Delegates_ReturnsSame_EarlyExit() {
		ParameterizedCronTabList testObject = new ParameterizedCronTabList(Arrays.asList(mockParameterizedCronTab,
				mockParameterizedCronTabToo));

		String sanityValue = "foo";
		when(mockParameterizedCronTab.checkSanity()).thenReturn(sanityValue);
		assertSame(sanityValue, testObject.checkSanity());

		verifyNoInteractions(mockParameterizedCronTabToo);
	}

	@Test
	void checkSanity_Delegates_ReturnsSame() {
		ParameterizedCronTabList testObject = new ParameterizedCronTabList(Arrays.asList(mockParameterizedCronTab,
				mockParameterizedCronTabToo));

		String sanityValue = "foo";
		when(mockParameterizedCronTabToo.checkSanity()).thenReturn(sanityValue);
		assertSame(sanityValue, testObject.checkSanity());
	}

	@Test
	void create_with_timezone() {
		ParameterizedCronTabList testObject = ParameterizedCronTabList.create("TZ=Australia/Sydney \n * * * * *%foo=bar");
		assertTrue(testObject.checkSanity().startsWith("Do you really mean \"every minute\""), testObject.checkSanity());
		List<ParameterizedCronTab> actualCronTabs = testObject.check(new GregorianCalendar());
		assertThat(actualCronTabs.size(), is(1));

		Map<String, String> expected = Collections.singletonMap("foo", "bar");
		assertEquals(expected, actualCronTabs.get(0).getParameterValues());
	}

	@Test
	void create_with_invalidTimezone() {
		assertThrows(IllegalArgumentException.class, () ->
			ParameterizedCronTabList.create("TZ=Dune/Arrakis \n * * * * *%foo=bar"));
	}

}
