# Alpha's Everything Addon (AEA)

Made by AlphaSucks.

AEA contains code and assets by other authors:

* **Sync** by Overgrown — MIT License (see LICENSE_sync). Shipped with its original
  `sync:` namespace; bug fixes by AEA are marked in the changelog.
* **Apoli / Origins (Overgrown's reimplementation)** by Overgrown — The A/O License v1
  (https://0vergrown.github.io/Handbook/license). Parts of it are ported (altered) to run
  on top of apace100's Apoli 2.9.2 / Origins 1.10.2. Ported types keep the `apoli:` and
  `origins:` namespaces. This is a fork under clause 5.3 with a distinct name and mod id.
* **Origins: Classes** 1.7.0 by apace100 — MIT License (see LICENSE_origins_classes). Bundled with
  its original `origins-classes:` namespace and package; AEA fixes are marked `AEA:` in the source.
  Remove the standalone Origins: Classes jar when using AEA.
* **Carry On Ledger Logs** by Lunascape — CC0 1.0. Backported from 1.21.4 to 1.20.1 and built in: when both
  Carry On and Ledger are installed, blocks picked up / put down with Carry On are logged to Ledger.
* **Function Macro Backport** by ThatRobin — MIT License (see LICENSE_function_macro_backport). 1.20.2 function
  macros (`$` lines, `/function <name> <arguments>` / `with`, `\` line continuation), rewritten to chain with
  vanilla instead of replacing the function parser and /function. Remove the standalone jar when using AEA.
* **Apoli / Origins** by apace100 — MIT License; AEA depends on them, it does not bundle them.

Source for AEA is distributed with the mod (sources jar) as required by clause 5.2 of the A/O License.
