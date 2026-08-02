local monitor = peripheral.find("monitor")
local reactor = peripheral.wrap("back")
local out = monitor or term
while true do
  out.clear(); out.setCursorPos(1, 1)
  local heat = reactor.getHeat and reactor.getHeat() or 0
  local maximum = reactor.getMaximumHeat and reactor.getMaximumHeat() or 10000
  out.write(string.format("Reactor heat: %d / %d", heat, maximum))
  redstone.setOutput("right", heat >= maximum * 0.70)
  sleep(1)
end
