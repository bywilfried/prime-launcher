# External shortcut choice

On Android 8+, ask Quick Search to add an installed application to Home.
- Choose Add with Prime: verify the real app icon/icon pack, Home label and size defaults,
  correct launch target, individual editing, persistence after launcher restart, and
  independence after uninstalling Quick Search.
- Choose Keep: verify the original shortcut destination still opens.
- Cancel or press Back: no icon or shortcut metadata should be created.

From another publisher (or a Quick Search contact/file shortcut), choose an installed
application: search by name, select it, confirm, and verify the selected app is added.
Cancel the picker to return to the original choice. Test rotation in both dialogs.
Test personal and work copies of an app; verify the selected profile launches.

Test a browser web shortcut using Keep; it must open the site, not just the browser.
Test the legacy INSTALL_SHORTCUT receiver: it should show the same choice, preserve
its destination when kept, and support conversion of MAIN/LAUNCHER app intents.

Fill page 0 including widgets: addition must use a free cell on another page.
Fill every supported page: addition must fail without overlapping any existing item.
Cancel the request in the source app before confirming: no icon should be created.

Automated: ShortcutAppTargetTest checks known/unknown shortcut ID conventions and
placement on occupied/full grids. Android build and device tests remain necessary.
