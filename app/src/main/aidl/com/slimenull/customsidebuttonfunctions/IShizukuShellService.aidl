package com.slimenull.customsidebuttonfunctions;

import com.slimenull.customsidebuttonfunctions.IShizukuShellCallback;

interface IShizukuShellService {
    void execute(String command, IShizukuShellCallback callback) = 1;
    void destroy() = 16777114;
}
