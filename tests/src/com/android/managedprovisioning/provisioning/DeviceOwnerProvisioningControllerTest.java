/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.managedprovisioning.provisioning;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import android.content.ComponentName;
import android.test.suitebuilder.annotation.SmallTest;

import com.android.managedprovisioning.model.PackageDownloadInfo;
import com.android.managedprovisioning.model.ProvisioningParams;
import com.android.managedprovisioning.model.WifiInfo;
import com.android.managedprovisioning.task.AbstractProvisioningTask;
import com.android.managedprovisioning.task.AddWifiNetworkTask;
import com.android.managedprovisioning.task.DeleteNonRequiredAppsTask;
import com.android.managedprovisioning.task.DeviceOwnerInitializeProvisioningTask;
import com.android.managedprovisioning.task.DisallowAddUserTask;
import com.android.managedprovisioning.task.DownloadPackageTask;
import com.android.managedprovisioning.task.InstallPackageTask;
import com.android.managedprovisioning.task.SetDevicePolicyTask;

import org.mockito.Mock;

/**
 * Unit tests for {@link DeviceOwnerProvisioningController}.
 */
public class DeviceOwnerProvisioningControllerTest extends ProvisioningControllerBaseTest {

    private static final int TEST_USER_ID = 123;
    private static final ComponentName TEST_ADMIN = new ComponentName("com.test.admin",
            "com.test.admin.AdminReceiver");

    private static final String TEST_SSID = "SomeSsid";
    private static final WifiInfo TEST_WIFI_INFO = new WifiInfo.Builder()
            .setSsid(TEST_SSID)
            .build();

    private static final String TEST_DOWNLOAD_LOCATION = "http://www.some.uri.com";
    private static final byte[] TEST_PACKAGE_CHECKSUM = new byte[] { '1', '2', '3', '4', '5' };
    private static final PackageDownloadInfo TEST_DOWNLOAD_INFO = new PackageDownloadInfo.Builder()
            .setLocation(TEST_DOWNLOAD_LOCATION)
            .setSignatureChecksum(TEST_PACKAGE_CHECKSUM)
            .build();

    @Mock private AbstractProvisioningController.ProvisioningServiceInterface mService;

    @SmallTest
    public void testRunAllTasks() throws Exception {
        // GIVEN device owner provisioning was invoked with a wifi and download info
        createController(TEST_WIFI_INFO, TEST_DOWNLOAD_INFO);

        // WHEN starting the test run
        mController.start();

        // THEN the initialization task is run first
        taskSucceeded(DeviceOwnerInitializeProvisioningTask.class);

        // THEN the add wifi task should be run
        taskSucceeded(AddWifiNetworkTask.class);

        // THEN the download package task should be run
        taskSucceeded(DownloadPackageTask.class);

        // THEN the install package task should be run
        taskSucceeded(InstallPackageTask.class);

        // THEN the delete non-required apps task should be run
        taskSucceeded(DeleteNonRequiredAppsTask.class);

        // THEN the set device policy task should be run
        taskSucceeded(SetDevicePolicyTask.class);

        // THEN the disallow add user task should be run
        taskSucceeded(DisallowAddUserTask.class);

        // THEN the provisioning complete callback should have happened
        verify(mService).provisioningComplete();
    }

    @SmallTest
    public void testNoWifiInfo() throws Exception {
        // GIVEN device owner provisioning was invoked with a wifi and download info
        createController(null, TEST_DOWNLOAD_INFO);

        // WHEN starting the test run
        mController.start();

        // THEN the initialization task is run first
        taskSucceeded(DeviceOwnerInitializeProvisioningTask.class);

        // THEN the download package task should be run
        taskSucceeded(DownloadPackageTask.class);

        // THEN the install package task should be run
        taskSucceeded(InstallPackageTask.class);

        // THEN the delete non-required apps task should be run
        taskSucceeded(DeleteNonRequiredAppsTask.class);

        // THEN the set device policy task should be run
        taskSucceeded(SetDevicePolicyTask.class);

        // THEN the disallow add user task should be run
        taskSucceeded(DisallowAddUserTask.class);

        // THEN the provisioning complete callback should have happened
        verify(mService).provisioningComplete();
    }

    @SmallTest
    public void testNoDownloadInfo() throws Exception {
        // GIVEN device owner provisioning was invoked with a wifi and download info
        createController(TEST_WIFI_INFO, null);

        // WHEN starting the test run
        mController.start();

        // THEN the initialization task is run first
        taskSucceeded(DeviceOwnerInitializeProvisioningTask.class);

        // THEN the add wifi task should be run
        taskSucceeded(AddWifiNetworkTask.class);

        // THEN the delete non-required apps task should be run
        taskSucceeded(DeleteNonRequiredAppsTask.class);

        // THEN the set device policy task should be run
        taskSucceeded(SetDevicePolicyTask.class);

        // THEN the disallow add user task should be run
        taskSucceeded(DisallowAddUserTask.class);

        // THEN the provisioning complete callback should have happened
        verify(mService).provisioningComplete();
    }

    @SmallTest
    public void testErrorAddWifiTask() throws Exception {
        // GIVEN device owner provisioning was invoked with a wifi and download info
        createController(TEST_WIFI_INFO, TEST_DOWNLOAD_INFO);

        // WHEN starting the test run
        mController.start();

        // THEN the initialization task is run first
        taskSucceeded(DeviceOwnerInitializeProvisioningTask.class);

        // THEN the add wifi task should be run
        AbstractProvisioningTask task = verifyTaskRun(AddWifiNetworkTask.class);

        // WHEN the task causes an error
        mController.onError(task, 0);

        // THEN the onError callback should have been called without factory reset being required
        verify(mService).error(anyInt(), eq(false));
    }

    @SmallTest
    public void testErrorDownloadAppTask() throws Exception {
        // GIVEN device owner provisioning was invoked with a wifi and download info
        createController(TEST_WIFI_INFO, TEST_DOWNLOAD_INFO);

        // WHEN starting the test run
        mController.start();

        // THEN the initialization task is run first
        taskSucceeded(DeviceOwnerInitializeProvisioningTask.class);

        // THEN the add wifi task should be run
        taskSucceeded(AddWifiNetworkTask.class);

        // THEN the download package task should be run
        AbstractProvisioningTask task = verifyTaskRun(DownloadPackageTask.class);

        // WHEN the task causes an error
        mController.onError(task, 0);

        // THEN the onError callback should have been called with factory reset being required
        verify(mService).error(anyInt(), eq(true));
    }

    private void createController(WifiInfo wifiInfo, PackageDownloadInfo downloadInfo) {
        ProvisioningParams params = new ProvisioningParams.Builder()
                .setDeviceAdminComponentName(TEST_ADMIN)
                .setProvisioningAction(ACTION_PROVISION_MANAGED_DEVICE)
                .setWifiInfo(wifiInfo)
                .setDeviceAdminDownloadInfo(downloadInfo)
                .build();

        mController = new DeviceOwnerProvisioningController(
                getContext(),
                params,
                TEST_USER_ID,
                mService,
                mHandler);
        mController.initialize();
    }
}