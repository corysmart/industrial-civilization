local reactor = peripheral.wrap("back")
local heat = reactor.getHeat and reactor.getHeat() or 0
local maximum = reactor.getMaximumHeat and reactor.getMaximumHeat() or 10000
if heat >= maximum * 0.70 then
  redstone.setOutput("right", true)
  print("SCRAM asserted: external redstone disables reactor")
else
  print("Heat within configured limit")
end
