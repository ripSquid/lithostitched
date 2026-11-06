> [!WARNING]
> **This is an unofficial fork of [Apollounknowndev/lithostitched](https://github.com/Apollounknowndev/lithostitched). It is not the real mod and is not affiliated with or endorsed by its author.**
> If you just want Lithostitched, get it from [Modrinth](https://modrinth.com/mod/lithostitched).
>
> **What this fork is:** upstream Lithostitched plus small performance patches, published so our servers can pin an exact, hash-verified jar. Each release is upstream's code at a named commit with the patch on top, and the mod id is unchanged (`lithostitched`), so it is a drop-in replacement.
>
> **Current patch (`2.0.6+perf.1`, Minecraft 26.3, Fabric):** the biome-timeline mixin on `EnvironmentAttributeSystem$ValueSampler` rebuilt two stream pipelines and two lists on every environment attribute lookup. That showed up as ~2.5% of the server thread in a spark profile, mostly during mob spawning. The layer split is now computed once per sampler and reused. Behaviour is otherwise unchanged. The change is on the [`perf/skip-timeline-layers`](https://github.com/ripSquid/lithostitched/tree/perf/skip-timeline-layers) branch and downloads are under [Releases](https://github.com/ripSquid/lithostitched/releases).
>
> **Issues with this fork's builds go here, not to upstream.** The plan is to offer the change upstream as a pull request; if it is merged there, this fork's build becomes unnecessary.
>
> **AI disclosure:** the patch was written with AI assistance (Claude) and reviewed by a human. Everything else is upstream's code.
>
> Original work copyright (c) 2023 Apollo, MIT licensed (see [LICENSE](LICENSE)).

# Lithostitched

Lithostitched is a library mod with new configurability and compatibility enhancements for worldgen.

## Description

This mod introduces new tools for datapack and modded worldgen developers to utilize, with the aim of increasing inter-project compatibility and expanding configurability in general. Currently, there are features such as:
- Biome modifiers
- Template pool injectors
- Surface rule modifiers

All of these new capabilities are **data-driven**! Take a look at the wiki page for more information.


*(Code for the mod is accessible in the other branches.)*
