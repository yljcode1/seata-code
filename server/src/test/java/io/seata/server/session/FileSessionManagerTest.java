/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.server.session;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import io.seata.common.XID;
import io.seata.core.model.BranchStatus;
import io.seata.core.model.BranchType;
import io.seata.core.model.GlobalStatus;
import io.seata.server.storage.file.session.FileSessionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;


/**
 * The type File based session manager test.
 *
 * @author tianming.xm @gmail.com
 * @since 2019 /1/22
 */
@SpringBootTest
public class FileSessionManagerTest {


    private static List<SessionManager> sessionManagerList;


    @BeforeAll
    public static void setUp(ApplicationContext context){
        try {
            sessionManagerList = Arrays.asList(new FileSessionManager("root.data", "."),
                new FileSessionManager("test", null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add global session test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void addGlobalSessionTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            sessionManager.removeGlobalSession(globalSession);
        }
    }

    /**
     * Find global session test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void findGlobalSessionTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            GlobalSession expected = sessionManager.findGlobalSession(globalSession.getXid());
            Assertions.assertNotNull(expected);
            Assertions.assertEquals(expected.getTransactionId(), globalSession.getTransactionId());
            Assertions.assertEquals(expected.getApplicationId(), globalSession.getApplicationId());
            Assertions.assertEquals(expected.getTransactionServiceGroup(), globalSession.getTransactionServiceGroup());
            Assertions.assertEquals(expected.getTransactionName(), globalSession.getTransactionName());
            Assertions.assertEquals(expected.getTransactionId(), globalSession.getTransactionId());
            Assertions.assertEquals(expected.getStatus(), globalSession.getStatus());
            sessionManager.removeGlobalSession(globalSession);
        }
    }

    /**
     * Update global session status test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void updateGlobalSessionStatusTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            globalSession.setStatus(GlobalStatus.Finished);
            sessionManager.updateGlobalSessionStatus(globalSession, GlobalStatus.Finished);
            GlobalSession expected = sessionManager.findGlobalSession(globalSession.getXid());
            Assertions.assertNotNull(expected);
            Assertions.assertEquals(GlobalStatus.Finished, expected.getStatus());
            sessionManager.removeGlobalSession(globalSession);
        }
    }

    /**
     * Remove global session test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void removeGlobalSessionTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            sessionManager.removeGlobalSession(globalSession);
            GlobalSession expected = sessionManager.findGlobalSession(globalSession.getXid());
            Assertions.assertNull(expected);
        }
    }

    /**
     * Add branch session test.
     *
     * @param globalSession the global session
     * @param branchSession the branch session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("branchSessionProvider")
    public void addBranchSessionTest(GlobalSession globalSession, BranchSession branchSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            sessionManager.addBranchSession(globalSession, branchSession);
            sessionManager.removeBranchSession(globalSession, branchSession);
            sessionManager.removeGlobalSession(globalSession);
        }
    }

    /**
     * Update branch session status test.
     *
     * @param globalSession the global session
     * @param branchSession the branch session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("branchSessionProvider")
    public void updateBranchSessionStatusTest(GlobalSession globalSession, BranchSession branchSession)
            throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            sessionManager.addBranchSession(globalSession, branchSession);
            sessionManager.updateBranchSessionStatus(branchSession, BranchStatus.PhaseTwo_Committed);
            sessionManager.removeBranchSession(globalSession, branchSession);
            sessionManager.removeGlobalSession(globalSession);
        }
    }

    /**
     * Remove branch session test.
     *
     * @param globalSession the global session
     * @param branchSession the branch session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("branchSessionProvider")
    public void removeBranchSessionTest(GlobalSession globalSession, BranchSession branchSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.addGlobalSession(globalSession);
            sessionManager.addBranchSession(globalSession, branchSession);
            sessionManager.removeBranchSession(globalSession, branchSession);
            sessionManager.removeGlobalSession(globalSession);
        }
    }

    /**
     * All sessions test.
     *
     * @param globalSessions the global sessions
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionsProvider")
    public void allSessionsTest(List<GlobalSession> globalSessions) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            for (GlobalSession globalSession : globalSessions) {
                sessionManager.addGlobalSession(globalSession);
            }
            Collection<GlobalSession> expectedGlobalSessions = sessionManager.allSessions();
            Assertions.assertNotNull(expectedGlobalSessions);
            Assertions.assertEquals(2, expectedGlobalSessions.size());
            for (GlobalSession globalSession : globalSessions) {
                sessionManager.removeGlobalSession(globalSession);
            }
        }
    }

    /**
     * Find global sessions test.
     *
     * @param globalSessions the global sessions
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionsProvider")
    public void findGlobalSessionsTest(List<GlobalSession> globalSessions) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            for (GlobalSession globalSession : globalSessions) {
                sessionManager.addGlobalSession(globalSession);
            }
            SessionCondition sessionCondition = new SessionCondition(30 * 24 * 3600);
            Collection<GlobalSession> expectedGlobalSessions = sessionManager.findGlobalSessions(sessionCondition);
            Assertions.assertNotNull(expectedGlobalSessions);
            Assertions.assertEquals(2, expectedGlobalSessions.size());
            for (GlobalSession globalSession : globalSessions) {
                sessionManager.removeGlobalSession(globalSession);
            }
        }
    }

    /**
     * On begin test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void onBeginTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * On status change test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void onStatusChangeTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onStatusChange(globalSession, GlobalStatus.Finished);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * On branch status change test.
     *
     * @param globalSession the global session
     * @param branchSession the branch session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("branchSessionProvider")
    public void onBranchStatusChangeTest(GlobalSession globalSession, BranchSession branchSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onAddBranch(globalSession, branchSession);
            sessionManager.onBranchStatusChange(globalSession, branchSession, BranchStatus.PhaseTwo_Committed);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * On add branch test.
     *
     * @param globalSession the global session
     * @param branchSession the branch session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("branchSessionProvider")
    public void onAddBranchTest(GlobalSession globalSession, BranchSession branchSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onAddBranch(globalSession, branchSession);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * On remove branch test.
     *
     * @param globalSession the global session
     * @param branchSession the branch session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("branchSessionProvider")
    public void onRemoveBranchTest(GlobalSession globalSession, BranchSession branchSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onAddBranch(globalSession, branchSession);
            sessionManager.onRemoveBranch(globalSession, branchSession);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * On close test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void onCloseTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onClose(globalSession);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * On end test.
     *
     * @param globalSession the global session
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("globalSessionProvider")
    public void onEndTest(GlobalSession globalSession) throws Exception {
        for (SessionManager sessionManager : sessionManagerList) {
            sessionManager.onBegin(globalSession);
            sessionManager.onEnd(globalSession);
        }
    }

    /**
     * Global session provider object [ ] [ ].
     *
     * @return the object [ ] [ ]
     */
    static Stream<Arguments> globalSessionProvider() {
        GlobalSession globalSession = new GlobalSession("demo-app", "my_test_tx_group", "test", 6000);

        String xid = XID.generateXID(globalSession.getTransactionId());
        globalSession.setXid(xid);

        return Stream.of(
                Arguments.of(globalSession)
        );
    }

    /**
     * Global sessions provider object [ ] [ ].
     *
     * @return the object [ ] [ ]
     */
    static Stream<Arguments> globalSessionsProvider() {
        GlobalSession globalSession1 = new GlobalSession("demo-app", "my_test_tx_group", "test", 6000);
        GlobalSession globalSession2 = new GlobalSession("demo-app", "my_test_tx_group", "test", 6000);
        return Stream.of(
                Arguments.of(Arrays.asList(globalSession1, globalSession2))
        );
    }

    /**
     * Branch session provider object [ ] [ ].
     *
     * @return the object [ ] [ ]
     */
    static Stream<Arguments> branchSessionProvider() {
        GlobalSession globalSession = new GlobalSession("demo-app", "my_test_tx_group", "test", 6000);
        globalSession.setXid(XID.generateXID(globalSession.getTransactionId()));
        BranchSession branchSession = new BranchSession();
        branchSession.setTransactionId(globalSession.getTransactionId());
        branchSession.setBranchId(1L);
        branchSession.setResourceGroupId("my_test_tx_group");
        branchSession.setResourceId("tb_1");
        branchSession.setLockKey("t_1");
        branchSession.setBranchType(BranchType.AT);
        branchSession.setApplicationData("{\"data\":\"test\"}");
        return Stream.of(
                Arguments.of(globalSession, branchSession)
        );
    }
}
