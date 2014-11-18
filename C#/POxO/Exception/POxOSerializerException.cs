/*
 * Copyright 2014 Giuseppe Gerla. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using System;

public class POxOSerializerException : Exception
{

    /**
     * Instantiates a new event exception.
     * 
     * @param message message of the exception
     */
    public POxOSerializerException(String message)
        : base(message)
    {
    }

    /**
     * Instantiates a new event exception.
     * 
     * @param message message of the exception
     * @param e original exception
     */
    public POxOSerializerException(String message, Exception e)
        : base(message, e)
    {
    }
}

