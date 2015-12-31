Sub Sketch2Face2()
'J.Kriek 2012
    Dim oApp As Application
    Set oApp = ThisApplication
    Dim oPart As PartDocument
    Set oPart = oApp.ActiveDocument
    Dim oCompDef As ComponentDefinition
    Set oCompDef = oPart.ComponentDefinition
    Dim oFaces As Faces
    Dim oFace As Face
    Dim oEdge As Edge
    Dim x As Double
    Dim y As Double
    Dim z As Double
    
    Dim x1 As Double
    Dim y1 As Double
    Dim z1 As Double
    
    Open "c:\temp\data.csv" For Output As #1
    
    Dim objUOM As UnitsOfMeasure
    Dim oCOM As Point
    
    Set objUOM = oPart.UnitsOfMeasure
    Set oCOM = oCompDef.MassProperties.CenterOfMass

    cogx = objUOM.GetStringFromValue(oCOM.x, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogy = objUOM.GetStringFromValue(oCOM.y, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogz = objUOM.GetStringFromValue(oCOM.z, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    
    'This can be changed to .SideFaces, .EndFaces, or .StartFaces
    'But .Faces will let you choose between all faces
    Set oFaces = oCompDef.SurfaceBodies.Item(1).Faces
    
    Set oFaces = oCompDef.SurfaceBodies.Item(1).Faces
    For Each oFace In oFaces
        For Each oEdge In oFace.Edges
            x = oEdge.StartVertex.Point.x - oCOM.x
            y = oEdge.StartVertex.Point.y - oCOM.y
            z = oEdge.StartVertex.Point.z - oCOM.z
            
            x1 = oEdge.StopVertex.Point.x - oCOM.x
            y1 = oEdge.StopVertex.Point.y - oCOM.y
            z1 = oEdge.StopVertex.Point.z - oCOM.z
            
            If oEdge.GeometryType = kCircularArcCurve Then
              Debug.Print "circle"
              
            
            End If
            
            Print #1, x & "," & y & "," & z & "#" & x1 & "," & y1 & "," & z1
        Next
    Next
    Close #1
    
    Dim oSketch As PlanarSketch

    'Put a sketch on the first face (1) - change to suit
    'Dim oSketches As Sketches3D
    'Set oSketch = oCompDef.Sketches.Add(oFaces(3), True)
End Sub
