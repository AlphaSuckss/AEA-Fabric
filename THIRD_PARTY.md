# Third-party code and assets

AEA is MIT licensed (see `LICENSE`), except for the code ported from Overgrown's Apoli and Origins. It includes material
from these projects; their notices ship in the jar as `LICENSE_*`:

- **Apoli and Origins (Overgrown's reimplementation)** by Overgrown, under the A/O License v1
  (https://0vergrown.github.io/Handbook/license): ported and changed to run on Apoli 2.9.2 / Origins 1.10.2
  (`port/`, `originsport/`; `LICENSE_overgrown_apoli`). AEA is a fork under clause 5.3, with its own name and mod id,
  and its source is published as clause 5.2 requires.
- **Sync 1.11.8** by Overgrown, MIT: bundled with its `sync:` namespace; AEA's fixes are in `dev/overgrown/sync/`
  (`LICENSE_sync`).
- **Origins: Classes 1.7.0** by apace100, MIT: bundled with its `origins-classes:` namespace and package; AEA's fixes
  are in `io/github/apace100/originsclasses/` (`LICENSE_origins_classes`).
- **Function Macro Backport** by ThatRobin, MIT: 1.20.2 function macros, rewritten to work alongside vanilla's parser
  (`macro/`; `LICENSE_function_macro_backport`).
- **Carry On Ledger Logs** by Lunascape, CC0 1.0: backported from 1.21.4 (`compat/carryonledger/`).

Apoli 2.9.2 and Origins 1.10.2 by apace100 are dependencies, not included.
