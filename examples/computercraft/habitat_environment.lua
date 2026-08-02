local env = peripheral.wrap("back")
while true do
  term.clear(); term.setCursorPos(1, 1)
  print("Oxygen: " .. tostring(env.getOxygen and env.getOxygen() or "use GC oxygen detector"))
  print("Radiation: " .. tostring(env.getRadiation and env.getRadiation() or "adapter unavailable"))
  print("Pressure: " .. tostring(env.getPressure and env.getPressure() or "GC sealed/unsealed state"))
  sleep(3)
end
