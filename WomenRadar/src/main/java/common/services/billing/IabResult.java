/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common.services.billing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the result of an in-app billing operation.
 * A result is composed of a response code (an integer) and possibly a
 * message (String). You can get those by calling
 * {@link #getResponse} and {@link #getMessage()}, respectively. You
 * can also inquire whether a result is a success or a failure by
 * calling {@link #isSuccess()} and {@link #isFailure()}.
 */
public class IabResult {
    private final int mResponse;
    private final String mMessage;

    public IabResult(int response, @Nullable String message) {
		this.mResponse = response;
		this.mMessage = message == null || message.trim().isEmpty() ? IabHelper.getResponseDesc(response) : message + " (response: " + IabHelper.getResponseDesc(response) + ')';
	}

    public int getResponse() {
        return this.mResponse;
    }

    public String getMessage() {
        return this.mMessage;
    }

    private boolean isSuccess() {
        return this.mResponse == IabHelper.BILLING_RESPONSE_RESULT_OK;
    }

    public boolean isFailure() {
        return !this.isSuccess();
    }

    @NonNull
    public String toString() {
        return "IabResult: " + this.getMessage();
    }
}
