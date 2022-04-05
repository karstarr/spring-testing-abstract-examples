package com.griddynamics.akarsakov.services;

import com.griddynamics.akarsakov.entities.Rocket;
import com.griddynamics.akarsakov.entities.Spaceport;
import com.griddynamics.akarsakov.utils.ChanceCalculator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unitTest")
class LauncherServiceImplTest {

    private final ChanceCalculator calculatorMock = Mockito.mock(ChanceCalculator.class);

    @Test
    void launchSuccessful() {
        Mockito.doReturn(11).when(calculatorMock).rollForLaunchSuccess();

        LauncherServiceImpl service = new LauncherServiceImpl(calculatorMock);

        assertTrue(service.launch(
                new Rocket(UUID.randomUUID(), "light"),
                new Spaceport(UUID.randomUUID(), 20.0, 70.0)
        ));
    }

    @Test
    void launchFailed() {
        Mockito.doReturn(10).when(calculatorMock).rollForLaunchSuccess();

        LauncherServiceImpl service = new LauncherServiceImpl(calculatorMock);

        assertFalse(service.launch(
                new Rocket(UUID.randomUUID(), "heavy"),
                new Spaceport(UUID.randomUUID(), 25.0, 90.0)
        ));
    }

    @Test
    void deliverRocketToSpaceport() {
        Spaceport port = new Spaceport(UUID.randomUUID(), 25.0, 90.0);
        Rocket rocket = new Rocket(UUID.randomUUID(), "heavy");

        LauncherServiceImpl launcherService = new LauncherServiceImpl(calculatorMock);
        launcherService.deliverRocketToSpaceport(rocket, port);

        assertEquals(port, rocket.getSpaceport());
    }
}