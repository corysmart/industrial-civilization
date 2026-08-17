package com.industrialcivilization.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuestPackMigrationTest {
    @Test
    public void recognizesCurrentAndLegacyPackIdentities() {
        assertTrue(QuestPackMigration.isPackIdentity("Industrial Civilization — Astra"));
        assertTrue(QuestPackMigration.isPackIdentity("Industrial Civilization — Phase 2"));
        assertFalse(QuestPackMigration.isPackIdentity("Unrelated Quest Pack"));
        assertFalse(QuestPackMigration.isPackIdentity(null));
    }
}
