// Scratch harness: time Engine::RunCycle over a project at a 10 Hz tick, faster than real time.
#include "xjmusic/Engine.h"
#include <algorithm>
#include <chrono>
#include <cstdio>
#include <iostream>
#include <numeric>
#include <string>
#include <vector>
using namespace XJ;
using clk = std::chrono::steady_clock;
static double ms(clk::time_point a, clk::time_point b) { return std::chrono::duration<double, std::milli>(b - a).count(); }
static double pct(std::vector<double> v, double p) { if (v.empty()) return 0; std::sort(v.begin(), v.end()); return v[std::min(v.size() - 1, (size_t)(p * (v.size() - 1)))]; }
static void report(const char *label, std::vector<double> v) {
  if (v.empty()) { std::printf("| %s | 0 | - | - | - | - | - |\n", label); return; }
  double mean = std::accumulate(v.begin(), v.end(), 0.0) / v.size();
  std::printf("| %s | %zu | %.2f | %.2f | %.2f | %.2f | %.2f |\n", label, v.size(), mean, pct(v, 0.5), pct(v, 0.95), pct(v, 0.99), *std::max_element(v.begin(), v.end()));
}
int main(int argc, char **argv) {
  if (argc < 2) { std::cerr << "usage: craft_timing <project.xj> [seconds] [craftAhead] [dubAhead] [overrideEverySec]\n"; return 1; }
  std::string path = argv[1];
  int seconds = argc > 2 ? std::stoi(argv[2]) : 600;
  int craftAhead = argc > 3 ? std::stoi(argv[3]) : 20;
  int dubAhead = argc > 4 ? std::stoi(argv[4]) : 10;
  int overrideEvery = argc > 5 ? std::stoi(argv[5]) : 0;
  auto t0 = clk::now();
  Engine engine(path, Fabricator::ControlMode::Auto, craftAhead, dubAhead, 1, 3600);
  auto t1 = clk::now();
  auto tmpl = engine.getProjectContent()->getFirstTemplate();
  if (!tmpl.has_value()) { std::cerr << "no template\n"; return 2; }
  engine.start(tmpl.value()->id);
  auto t2 = clk::now();
  std::vector<double> allCycles, craftCycles, idleCycles, rewriteCycles;
  size_t lastSegCount = 0; unsigned long long events = 0; int macroIdx = 0;
  auto macros = engine.getAllMacroPrograms();
  const unsigned long long step = 100000ULL; // 100 ms tick
  for (unsigned long long at = 0; at < (unsigned long long)seconds * 1000000ULL; at += step) {
    bool didOverride = false;
    if (overrideEvery > 0 && at > 0 && at % ((unsigned long long)overrideEvery * 1000000ULL) == 0 && !macros.empty()) {
      engine.doOverrideMacro(macros[macroIdx++ % macros.size()]); didOverride = true;
    }
    auto a = clk::now();
    auto evs = engine.RunCycle(at);
    auto b = clk::now();
    events += evs.size();
    double d = ms(a, b);
    size_t segCount = engine.getSegmentStore()->readAllSegments().size();
    allCycles.push_back(d);
    if (didOverride) rewriteCycles.push_back(d);
    else if (segCount != lastSegCount) craftCycles.push_back(d);
    else idleCycles.push_back(d);
    lastSegCount = segCount;
  }
  engine.finish(false);
  std::printf("project=%s simulated=%ds tick=100ms craftAhead=%ds dubAhead=%ds overrideEvery=%ds\n", path.c_str(), seconds, craftAhead, dubAhead, overrideEvery);
  std::printf("load content: %.1f ms   start(template): %.1f ms   schedule events: %llu\n", ms(t0, t1), ms(t1, t2), events);
  std::printf("\n| Cycle kind | n | mean ms | p50 | p95 | p99 | max |\n|---|---|---|---|---|---|---|\n");
  report("all cycles", allCycles);
  report("cycles that crafted a segment", craftCycles);
  report("cycles with macro override (rewrite)", rewriteCycles);
  report("idle cycles", idleCycles);
  return 0;
}
