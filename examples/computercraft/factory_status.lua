-- Wrap a Plethora/Computronics machine adapter or the Molecular Analyzer.
local p = peripheral.find("molecular_analyzer") or peripheral.wrap("back")
while true do
  term.clear(); term.setCursorPos(1, 1)
  local ok, status = pcall(function() return p.getStatus() end)
  print("Factory status: " .. (ok and tostring(status) or "adapter unavailable"))
  if p.getStored then print("Energy: " .. p.getStored() .. "/" .. p.getCapacity()) end
  sleep(2)
end
