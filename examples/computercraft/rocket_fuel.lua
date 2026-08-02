local telemetry = peripheral.wrap("back")
while true do
  local fuel = telemetry.getFuel and telemetry.getFuel() or "unavailable"
  local status = telemetry.getStatus and telemetry.getStatus() or "check Galacticraft telemetry link"
  print("Rocket: " .. tostring(status) .. " fuel=" .. tostring(fuel))
  sleep(2)
end
